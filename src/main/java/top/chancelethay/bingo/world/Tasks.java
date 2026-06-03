package top.chancelethay.bingo.world;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Thin wrapper around the Bukkit scheduler that gives the world-management classes a fluent
 * way to run sync/async work and to time-slice heavy main-thread operations.
 *
 * <p>Modeled after the same helper in the MineHunt project, which the world reset/generation
 * logic in this package is adapted from.
 */
public final class Tasks {

	private final Plugin plugin;

	public Tasks(Plugin plugin) {
		this.plugin = plugin;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	// ---------- sync (main thread) ----------

	/** Run a sync task on the next tick. */
	public void run(Runnable task) {
		Bukkit.getScheduler().runTask(plugin, task);
	}

	/** Run a sync task after a delay (in ticks). */
	public void later(Runnable task, long delayTicks) {
		Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(0L, delayTicks));
	}

	/** Repeat a sync task with a custom start delay and period (in ticks). */
	public BukkitTask repeat(Runnable task, long delayTicks, long periodTicks) {
		long delay = Math.max(0L, delayTicks);
		long period = Math.max(1L, periodTicks);
		return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
	}

	// ---------- async (background thread) ----------

	/** Run a task off the main thread (use only for IO/CPU work that does not touch the Bukkit API). */
	public void async(Runnable task) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
	}

	// ---------- management ----------

	public void cancel(BukkitTask task) {
		if (task == null) return;
		try {
			task.cancel();
		} catch (Throwable ignored) {
		}
	}

	/**
	 * Run a series of actions in order, spacing each one {@code delay} ticks apart, to avoid a
	 * single-tick stall when doing heavy main-thread work (e.g. successive world loads/unloads).
	 */
	public void runTasksInSequence(long delay, Runnable... actions) {
		runChainStep(Arrays.asList(actions).iterator(), delay);
	}

	private void runChainStep(Iterator<Runnable> it, long delay) {
		if (!it.hasNext()) return;
		try {
			it.next().run();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		later(() -> runChainStep(it, delay), delay);
	}
}
