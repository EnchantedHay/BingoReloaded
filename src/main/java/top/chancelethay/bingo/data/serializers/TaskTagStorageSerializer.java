package top.chancelethay.bingo.data.serializers;

import top.chancelethay.bingo.data.TaskTagData;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaskTagStorageSerializer implements DataStorageSerializer<TaskTagData.TaskTag> {

	@Override
	public void toDataStorage(@NotNull DataStorage storage, @NotNull TaskTagData.TaskTag value) {
		storage.setString("color", value.color().asHexString());
	}

	@Override
	public @Nullable TaskTagData.TaskTag fromDataStorage(@NotNull DataStorage storage) {
		return new TaskTagData.TaskTag(TextColor.fromHexString(storage.getString("color", "#808080")));
	}
}
