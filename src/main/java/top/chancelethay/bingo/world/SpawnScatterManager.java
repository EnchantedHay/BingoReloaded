package top.chancelethay.bingo.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Picks safe spawn spots for bingo participants and teleports them there asynchronously.
 *
 * <p>Adapted from the MineHunt project. Players are scattered around the world spawn on a ring
 * (radius/jitter configurable), keeping a minimum distance from each other. Candidate spots are
 * found using async chunk loading ({@link World#getChunkAtAsync}) so the main thread never blocks
 * on synchronous terrain generation, and they are filtered to avoid water, hazardous ground and
 * spots without head clearance.
 *
 * <p>This replaces the old synchronous {@code getRandomSpawnLocation} logic that teleported players
 * to fresh far-away coordinates inside a reused world. Because the world is now freshly regenerated
 * each round, scattering happens around spawn (0,0) instead.
 */
public final class SpawnScatterManager {

	private final Tasks tasks;

	private static final int MIN_RING_RADIUS = 8;
	private static final int MIN_PLAYER_DISTANCE_SQ = 24 * 24;

	private static final Set<Biome> WATER_BIOMES = Set.of(
			Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN,
			Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
			Biome.RIVER, Biome.FROZEN_RIVER
	);

	public SpawnScatterManager(Tasks tasks) {
		this.tasks = tasks;
	}

	/**
	 * Asynchronously find a safe spot per player on a ring around the world spawn and teleport them
	 * there. {@code onComplete} runs on the main thread once everyone has been placed.
	 *
	 * @param world      target world (its spawn is the ring centre)
	 * @param players    players to scatter
	 * @param ringRadius ring radius in blocks (clamped to a sane minimum)
	 * @param jitter     random +/- variation added to the radius
	 * @param maxTries   max attempts to find a safe spot per player before falling back to spawn
	 * @param onComplete callback after everyone is teleported, may be {@code null}
	 */
	public void performScatterAsync(World world, List<Player> players, int ringRadius, int jitter,
									int maxTries, Runnable onComplete) {
		if (world == null || players.isEmpty()) {
			if (onComplete != null) onComplete.run();
			return;
		}
		final int radius = Math.max(16, Math.max(MIN_RING_RADIUS, ringRadius));
		final int jit = Math.max(0, jitter);
		final int tries = Math.max(8, maxTries);

		processPlayersAsync(world, players, new ArrayList<>(), radius, jit, tries, locations -> tasks.run(() -> {
			for (int i = 0; i < players.size(); i++) {
				Location loc = i < locations.size() ? locations.get(i) : fallbackWorldSpawn(world);
				tpReset(players.get(i), loc);
			}
			if (onComplete != null) onComplete.run();
		}));
	}

	private void processPlayersAsync(World world, List<Player> players, List<Location> taken,
									 int radius, int jitter, int tries, Consumer<List<Location>> onAllDone) {
		int index = taken.size();
		if (index >= players.size()) {
			onAllDone.accept(taken);
			return;
		}
		findSingleSpotAsync(world, taken, radius, jitter, tries, loc -> {
			taken.add(loc);
			processPlayersAsync(world, players, taken, radius, jitter, tries, onAllDone);
		});
	}

	private void findSingleSpotAsync(World world, List<Location> taken, int radius, int jitter,
									 int triesLeft, Consumer<Location> callback) {
		if (triesLeft <= 0) {
			callback.accept(fallbackWorldSpawn(world));
			return;
		}
		int cx = world.getSpawnLocation().getBlockX();
		int cz = world.getSpawnLocation().getBlockZ();

		Random rnd = ThreadLocalRandom.current();
		double ang = rnd.nextDouble() * Math.PI * 2.0;
		int r = radius + (jitter <= 0 ? 0 : rnd.nextInt(-jitter, jitter + 1));
		if (r < MIN_RING_RADIUS) r = MIN_RING_RADIUS;

		int x = cx + (int) Math.round(Math.cos(ang) * r);
		int z = cz + (int) Math.round(Math.sin(ang) * r);

		world.getChunkAtAsync(new Location(world, x, 0, z)).thenAccept(chunk -> tasks.run(() -> {
			Location cand = toTopSafe(world, x, z);
			boolean valid = cand != null;
			if (valid) {
				for (Location used : taken) {
					if (used.distanceSquared(cand) < MIN_PLAYER_DISTANCE_SQ) {
						valid = false;
						break;
					}
				}
			}
			if (valid) {
				callback.accept(cand);
			} else {
				findSingleSpotAsync(world, taken, radius, jitter, triesLeft - 1, callback);
			}
		})).exceptionally(ex -> {
			tasks.run(() -> callback.accept(fallbackWorldSpawn(world)));
			return null;
		});
	}

	/**
	 * Resolve {@code (x,z)} to a safe standing spot: highest block, non-water biome, solid
	 * non-hazardous ground, sane height, two blocks of head clearance. Returns {@code null} if unsafe.
	 */
	private Location toTopSafe(World world, int x, int z) {
		int surfaceY = world.getHighestBlockYAt(x, z);
		Biome biome = world.getBiome(x, surfaceY, z);
		if (isWaterBiome(biome)) return null;

		Block top = world.getBlockAt(x, surfaceY, z);
		if (top.getType().isAir()) {
			top = world.getBlockAt(x, surfaceY - 1, z);
		}
		int y = top.getY();
		if (!withinSafeY(world, y)) return null;
		if (!isSolidGround(top.getType())) return null;
		if (isHazardGround(top.getType())) return null;

		if (!isClearSpace(world.getBlockAt(x, y + 1, z).getType())) return null;
		if (!isClearSpace(world.getBlockAt(x, y + 2, z).getType())) return null;

		return new Location(world, x + 0.5, y + 1.01, z + 0.5);
	}

	private boolean withinSafeY(World w, int y) {
		return switch (w.getEnvironment()) {
			case NORMAL -> y >= 54 && y <= 300;
			case NETHER -> y >= w.getMinHeight() + 6 && y <= w.getMaxHeight() - 6;
			case THE_END -> y >= 40 && y <= 300;
			default -> true;
		};
	}

	private Location fallbackWorldSpawn(World world) {
		Location s = world.getSpawnLocation();
		Location safe = toTopSafe(world, s.getBlockX(), s.getBlockZ());
		return safe != null ? safe : s.clone().add(0.5, 1, 0.5);
	}

	private void tpReset(Player p, Location loc) {
		try {
			p.teleport(loc);
			p.setFallDistance(0f);
			p.setFireTicks(0);
		} catch (Throwable ignored) {
		}
	}

	private boolean isWaterBiome(Biome b) {
		return b != null && WATER_BIOMES.contains(b);
	}

	private boolean isLiquid(Material m) {
		return m == Material.WATER || m == Material.LAVA;
	}

	private boolean isClearSpace(Material m) {
		if (m.isAir()) return true;
		if (isLiquid(m)) return false;
		return !m.isOccluding();
	}

	private boolean isSolidGround(Material m) {
		return m != null && !m.isAir() && m.isSolid() && !isLiquid(m);
	}

	private boolean isHazardGround(Material m) {
		return switch (m) {
			case SAND, RED_SAND, GRAVEL, CACTUS, CAMPFIRE, SOUL_CAMPFIRE,
				 MAGMA_BLOCK, SWEET_BERRY_BUSH, POWDER_SNOW -> true;
			default -> false;
		};
	}
}
