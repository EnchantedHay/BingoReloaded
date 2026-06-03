package top.chancelethay.bingo.data.serializers;

import top.chancelethay.bingo.data.DefaultKitData;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import top.chancelethay.bingo.lib.item.SerializableItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultKitStorageSerializer implements DataStorageSerializer<DefaultKitData.Kit> {

	@Override
	public void toDataStorage(@NotNull DataStorage storage, DefaultKitData.@NotNull Kit value) {
		storage.setSerializableList("items", SerializableItem.class, value.items());
	}

	@Override
	public @Nullable DefaultKitData.Kit fromDataStorage(@NotNull DataStorage storage) {

		List<SerializableItem> items = storage.getSerializableList("items", SerializableItem.class);
		return new DefaultKitData.Kit(items);
	}
}
