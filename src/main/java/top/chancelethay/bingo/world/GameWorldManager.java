package top.chancelethay.bingo.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Manages the lifecycle of the dedicated bingo game world (overworld + optional nether/end).
 *
 * <p>Adapted from the MineHunt project. Instead of reusing a single world and teleporting players
 * to fresh far-away coordinates each round (the old BingoReloaded approach), each round gets a
 * genuinely fresh, pre-generated world:
 * <ul>
 *     <li>{@link #prepareNextWorlds} builds a {@code <name>_next} world group in the background
 *     (random seed) and, when Chunky is present, pre-generates the spawn area so the first players
 *     don't trigger lag spikes. The {@code _next} worlds are then unloaded to free RAM.</li>
 *     <li>{@link #promoteWhenReady} unloads the current worlds, moves the {@code _next} folders into
 *     place and reloads them — making the pre-generated world the live game world.</li>
 * </ul>
 *
 * <p>All folder IO happens off the main thread; world (un)loads are time-sliced. Chunky is an
 * optional soft-dependency: without it, worlds are still regenerated, just without spawn preloading.
 */
public final class GameWorldManager {

	private final Tasks tasks;
	private final Logger log;

	private final String gameWorld;
	private final boolean createNether;
	private final boolean createEnd;
	private final int preloadRadiusBlocks;
	private final boolean randomSeedEachRound;

	private final AtomicBoolean resetting = new AtomicBoolean(false);
	private final AtomicBoolean nextPreparing = new AtomicBoolean(false);
	private final AtomicBoolean nextReady = new AtomicBoolean(false);
	private final AtomicInteger nextProgressPercent = new AtomicInteger(0);

	// Guards against stale Chunky callbacks from a previous generation round firing late.
	private final AtomicInteger generationId = new AtomicInteger(0);
	private final AtomicReference<BukkitTask> promoteTask = new AtomicReference<>();
	private static final int PROMOTE_TIMEOUT_TICKS = 20 * 60 * 10; // 10 minutes

	public GameWorldManager(Tasks tasks, String gameWorld, boolean createNether, boolean createEnd,
							int preloadRadiusBlocks, boolean randomSeedEachRound) {
		this.tasks = tasks;
		this.log = tasks.getPlugin().getLogger();
		this.gameWorld = gameWorld;
		this.createNether = createNether;
		this.createEnd = createEnd;
		this.preloadRadiusBlocks = Math.max(0, preloadRadiusBlocks);
		this.randomSeedEachRound = randomSeedEachRound;
	}

	// ---------- state queries ----------
	public boolean isResetting() { return resetting.get(); }
	public boolean isNextPreparing() { return nextPreparing.get(); }
	public boolean isNextReady() { return nextReady.get(); }
	public int getNextProgressPercent() { return Math.clamp(nextProgressPercent.get(), 0, 100); }
	public String gameWorldName() { return gameWorld; }

	/** Ensure the live game world group exists (create any missing dimension). */
	public void ensureWorlds() {
		ensureWorld(gameWorld, World.Environment.NORMAL);
		if (createNether) ensureWorld(gameWorld + "_nether", World.Environment.NETHER);
		if (createEnd) ensureWorld(gameWorld + "_the_end", World.Environment.THE_END);
	}

	// ========== background build of the _next world ==========

	/**
	 * Pre-generate the next round's world group in the background. Uses a CAS on {@code nextPreparing}
	 * so only one preparation runs at a time. When done the {@code _next} worlds are unloaded and
	 * {@code nextReady} is set, ready for {@link #promoteWhenReady}.
	 */
	public void prepareNextWorlds() {
		if (!nextPreparing.compareAndSet(false, true)) {
			return;
		}
		nextReady.set(false);
		nextProgressPercent.set(0);

		String base = gameWorld + "_next";
		tasks.runTasksInSequence(2L,
				() -> unloadIfLoaded(base, false),
				() -> unloadIfLoaded(base + "_nether", false),
				() -> unloadIfLoaded(base + "_the_end", false),
				() -> tasks.async(() -> {
					try {
						deleteWorldFolder(base);
						deleteWorldFolder(base + "_nether");
						deleteWorldFolder(base + "_the_end");
						long seed = randomSeedEachRound ? ThreadLocalRandom.current().nextLong() : 0L;
						tasks.run(() -> createNextWorldStep(base, seed, randomSeedEachRound));
					} catch (Throwable ex) {
						ex.printStackTrace();
						nextPreparing.set(false);
					}
				})
		);
	}

	private void createNextWorldStep(String base, long seed, boolean useSeed) {
		tasks.runTasksInSequence(5L,
				() -> createWorld(base, World.Environment.NORMAL, seed, useSeed),
				() -> { if (createNether) createWorld(base + "_nether", World.Environment.NETHER, seed, useSeed); },
				() -> { if (createEnd) createWorld(base + "_the_end", World.Environment.THE_END, seed, useSeed); },
				() -> {
					World w = Bukkit.getWorld(base);
					if (w == null) {
						nextPreparing.set(false);
						return;
					}
					if (preloadRadiusBlocks > 0 && isChunkyAvailable()) {
						startChunkyJob(w, preloadRadiusBlocks, () -> onNextPreloadDone(base));
					} else {
						onNextPreloadDone(base);
					}
				}
		);
	}

	// ========== promote (swap _next into place) ==========

	/**
	 * Promote the pre-generated {@code _next} world into the live game world. If it isn't ready yet,
	 * poll until it is (up to {@link #PROMOTE_TIMEOUT_TICKS}) then swap. {@code onDone} runs on the
	 * main thread once the new world is loaded.
	 */
	public void promoteWhenReady(Runnable onDone) {
		if (nextReady.get()) {
			doPromoteNow(onDone);
			return;
		}
		BukkitTask old = promoteTask.getAndSet(null);
		if (old != null) tasks.cancel(old);

		final int[] elapsed = {0};
		BukkitTask task = tasks.repeat(() -> {
			elapsed[0] += 40;
			if (resetting.get()) {
				BukkitTask t = promoteTask.getAndSet(null);
				if (t != null) tasks.cancel(t);
				return;
			}
			if (elapsed[0] >= PROMOTE_TIMEOUT_TICKS) {
				log.severe("[Worlds] promoteWhenReady timed out after 10 minutes! Aborting.");
				BukkitTask t = promoteTask.getAndSet(null);
				if (t != null) tasks.cancel(t);
				return;
			}
			if (nextReady.get()) {
				BukkitTask t = promoteTask.getAndSet(null);
				if (t != null) tasks.cancel(t);
				doPromoteNow(onDone);
			}
		}, 40L, 40L);
		promoteTask.set(task);
		log.info("[Worlds] Waiting for next world generation to finalize...");
	}

	private void doPromoteNow(Runnable onDone) {
		if (!resetting.compareAndSet(false, true)) return;

		tasks.runTasksInSequence(10L,
				() -> cancelChunkyJobsForWorld(gameWorld + "_next"),
				() -> unloadIfLoaded(gameWorld, false),
				() -> unloadIfLoaded(gameWorld + "_nether", false),
				() -> unloadIfLoaded(gameWorld + "_the_end", false),
				() -> unloadIfLoaded(gameWorld + "_next", true),
				() -> unloadIfLoaded(gameWorld + "_next_nether", true),
				() -> unloadIfLoaded(gameWorld + "_next_the_end", true),
				() -> startAsyncMove(onDone)
		);
	}

	private void startAsyncMove(Runnable onDone) {
		tasks.async(() -> {
			try {
				String gw = gameWorld;
				String nx = gameWorld + "_next";

				deleteWorldFolder(gw);
				deleteWorldFolder(gw + "_nether");
				deleteWorldFolder(gw + "_the_end");
				moveWorldFolder(nx, gw);
				if (createNether) moveWorldFolder(nx + "_nether", gw + "_nether");
				if (createEnd) moveWorldFolder(nx + "_the_end", gw + "_the_end");

				tasks.run(() -> tasks.runTasksInSequence(3L,
						() -> ensureWorld(gw, World.Environment.NORMAL),
						() -> { if (createNether) ensureWorld(gw + "_nether", World.Environment.NETHER); },
						() -> { if (createEnd) ensureWorld(gw + "_the_end", World.Environment.THE_END); },
						() -> {
							nextPreparing.set(false);
							nextReady.set(false);
							nextProgressPercent.set(100);
							resetting.set(false);
							safeRun(onDone);
							log.info("[Worlds] Promote finished, game world regenerated.");
						}
				));
			} catch (Throwable ex) {
				ex.printStackTrace();
				resetting.set(false);
			}
		});
	}

	// ---------- internals ----------

	private void ensureWorld(String name, World.Environment env) {
		if (Bukkit.getWorld(name) == null) {
			createWorld(name, env, 0L, false);
		}
	}

	private void createWorld(String name, World.Environment env, long seed, boolean useSeed) {
		try {
			WorldCreator wc = new WorldCreator(name).environment(env).type(WorldType.NORMAL);
			if (useSeed) wc.seed(seed);
			World w = Bukkit.createWorld(wc);
			if (w != null) {
				try {
					w.setAutoSave(true);
					int cx = w.getSpawnLocation().getBlockX() >> 4;
					int cz = w.getSpawnLocation().getBlockZ() >> 4;
					w.setChunkForceLoaded(cx, cz, true);
				} catch (Throwable ignored) {
				}
				log.info("[Worlds] Created: " + name + " (" + env.name() + ")");
			}
		} catch (Throwable ex) {
			log.severe("[Worlds] Create world failed: " + name + " -> " + ex.getMessage());
		}
	}

	private void unloadIfLoaded(String worldName, boolean save) {
		try {
			World w = Bukkit.getWorld(worldName);
			if (w == null) return;
			Bukkit.unloadWorld(w, save);
		} catch (Throwable ex) {
			log.warning("[Worlds] Unload error: " + worldName + " -> " + ex.getMessage());
		}
	}

	private void moveWorldFolder(String fromName, String toName) throws IOException {
		File container = Bukkit.getWorldContainer();
		Path src = new File(container, fromName).toPath();
		Path dst = new File(container, toName).toPath();
		if (!Files.exists(src)) return;
		Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		log.info("[Worlds] Moved folder: " + fromName + " -> " + toName);
	}

	private void deleteWorldFolder(String worldName) throws IOException {
		File container = Bukkit.getWorldContainer();
		File dir = new File(container, worldName);
		if (!dir.exists()) return;
		Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
			@Override
			public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
				try { Files.deleteIfExists(file); } catch (IOException ignored) {}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public @NotNull FileVisitResult postVisitDirectory(@NotNull Path d, IOException exc) {
				try { Files.deleteIfExists(d); } catch (IOException ignored) {}
				return FileVisitResult.CONTINUE;
			}
		});
		log.info("[Worlds] Deleted folder: " + worldName);
	}

	private void onNextPreloadDone(String base) {
		World w = Bukkit.getWorld(base);
		World wn = Bukkit.getWorld(base + "_nether");
		World we = Bukkit.getWorld(base + "_the_end");
		if (w != null) w.save();
		if (wn != null) wn.save();
		if (we != null) we.save();

		log.info("[Worlds] Next world ready, unloading to free RAM...");
		tasks.later(() -> {
			unloadIfLoaded(base, true);
			unloadIfLoaded(base + "_nether", true);
			unloadIfLoaded(base + "_the_end", true);
			nextProgressPercent.set(100);
			nextReady.set(true);
			nextPreparing.set(false);
		}, 40L);
	}

	private void safeRun(Runnable r) {
		if (r != null) try { r.run(); } catch (Throwable ignored) {}
	}

	// ---------- Chunky (optional soft-dependency) ----------

	private boolean isChunkyAvailable() {
		return Bukkit.getPluginManager().isPluginEnabled("Chunky")
				&& Bukkit.getServer().getServicesManager().load(ChunkyAPI.class) != null;
	}

	private void startChunkyJob(World world, int radiusBlocks, Runnable onComplete) {
		ChunkyAPI chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
		if (chunky == null) {
			onComplete.run();
			return;
		}
		final String worldName = world.getName();
		final int r = Math.max(0, radiusBlocks);
		final int thisGeneration = generationId.incrementAndGet();

		boolean started = chunky.startTask(worldName, "circle", 0.0, 0.0, r, r, "concentric");
		if (!started) {
			log.warning("[Worlds] Chunky refused to start a task for " + worldName + ", skipping preload.");
			onComplete.run();
			return;
		}

		chunky.onGenerationProgress((GenerationProgressEvent ev) -> {
			if (generationId.get() != thisGeneration) return;
			if (!worldName.equalsIgnoreCase(ev.world())) return;
			int pct = Math.clamp(Math.round(ev.progress()), 0, 100);
			if (pct > nextProgressPercent.get()) nextProgressPercent.set(pct);
		});
		chunky.onGenerationComplete((GenerationCompleteEvent ev) -> {
			if (generationId.get() != thisGeneration) return;
			if (!worldName.equalsIgnoreCase(ev.world())) return;
			tasks.run(() -> {
				try { world.save(); } catch (Throwable ignored) {}
				safeRun(onComplete);
			});
		});
	}

	private void cancelChunkyJobsForWorld(String worldName) {
		if (!isChunkyAvailable()) return;
		try {
			ChunkyAPI chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
			if (chunky != null && chunky.isRunning(worldName)) {
				chunky.cancelTask(worldName);
			}
		} catch (Throwable ignored) {
		}
	}
}
