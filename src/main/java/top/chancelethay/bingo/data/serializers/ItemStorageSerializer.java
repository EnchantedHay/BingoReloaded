package top.chancelethay.bingo.data.serializers;

import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import top.chancelethay.bingo.lib.item.SerializableItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStorageSerializer implements DataStorageSerializer<SerializableItem>
{
    @Override
    public void toDataStorage(@NotNull DataStorage storage, @NotNull SerializableItem value) {
        storage.setInt("slot", value.slot());
        storage.setItemStack("stack", value.stack());
    }

    @Override
    public @Nullable SerializableItem fromDataStorage(@NotNull DataStorage storage) {
        return new SerializableItem(storage.getInt("slot", 0), storage.getItemStack("stack"));
    }
}
