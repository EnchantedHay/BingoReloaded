package top.chancelethay.bingo.lib.platform;

import org.bukkit.scheduler.BukkitTask;

public final class ExtensionTask {

	private BukkitTask task;

	public void setTask(BukkitTask task) {
		this.task = task;
	}

	public boolean isCancelled() {
		return task.isCancelled();
	}

	public boolean isSync() {
		return task.isSync();
	}

	public void cancel() {
		task.cancel();
	}
}
