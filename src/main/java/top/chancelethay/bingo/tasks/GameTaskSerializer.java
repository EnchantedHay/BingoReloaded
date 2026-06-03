package top.chancelethay.bingo.tasks;

import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import top.chancelethay.bingo.tasks.data.TaskData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GameTaskSerializer implements DataStorageSerializer<GameTask> {

	@Override
	public void toDataStorage(@NotNull DataStorage storage, @NotNull GameTask value) {
		storage.setBoolean("voided", value.isVoided());
		storage.setUUID("completed_by", value.getCompletedByPlayer().isPresent() ? value.getCompletedByPlayer().get().getId() : null);
		storage.setLong("completed_at", value.completedAt);
		storage.setSerializable("task", TaskData.class, value.data);
	}

	@Override
	public @Nullable GameTask fromDataStorage(@NotNull DataStorage storage) {

		boolean voided = storage.getBoolean("voided", false);
		UUID completedByUUID = storage.getUUID("completed_by");
		long timeStr = storage.getLong("completed_at", -1L);
		TaskData data = storage.getSerializable("task", TaskData.class);
		GameTask task = new GameTask(data);

		task.setVoided(voided);
		task.completedAt = timeStr;
		//TODO: implement completedBy deserialization (need access to teamManager to get participant object).

		return task;
	}
}
