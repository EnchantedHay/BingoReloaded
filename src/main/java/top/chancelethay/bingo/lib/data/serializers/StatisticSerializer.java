package top.chancelethay.bingo.lib.data.serializers;

import top.chancelethay.bingo.lib.api.EntityType;
import top.chancelethay.bingo.lib.api.StatisticHandle;
import top.chancelethay.bingo.lib.api.StatisticType;
import top.chancelethay.bingo.lib.api.item.ItemType;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatisticSerializer implements DataStorageSerializer<StatisticHandle> {

	@Override
	public void toDataStorage(@NotNull DataStorage storage, @NotNull StatisticHandle value) {
		storage.setNamespacedKey("stat_type", value.statisticType().key());

		ItemType item = value.itemType();
		if (item != null)
		{
			storage.setNamespacedKey("item", item.key());
		}
		EntityType entity = value.entityType();
		if (entity != null)
		{
			storage.setNamespacedKey("entity", entity.key());
		}
	}

	@Override
	public @Nullable StatisticHandle fromDataStorage(@NotNull DataStorage storage) {
		StatisticType type = StatisticType.of(storage.getNamespacedKey("stat_type"));

		ItemType item = null;
		if (storage.contains("item"))
		{
			item = ItemType.of(storage.getNamespacedKey("item"));
		}
		EntityType entity = null;
		if (storage.contains("entity"))
		{
			entity = EntityType.of(storage.getNamespacedKey("entity"));
		}

		return StatisticHandle.create(type, item, entity);
	}
}
