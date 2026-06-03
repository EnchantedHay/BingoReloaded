package top.chancelethay.bingo.data.updater;

import top.chancelethay.bingo.BingoReloadedPaper;
import top.chancelethay.bingo.data.serializers.ItemStorageSerializer;
import top.chancelethay.bingo.item.BingoItems;
import top.chancelethay.bingo.item.GoUpWand;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializerRegistry;
import top.chancelethay.bingo.lib.data.core.tag.TagDataAccessor;
import top.chancelethay.bingo.lib.item.SerializableItem;

import java.util.ArrayList;
import java.util.List;

public class DataUpdaterV3_5_0 extends DataUpdaterV3_3_0 {

	public DataUpdaterV3_5_0(BingoReloadedPaper plugin) {
		super(plugin);
	}

	@Override
	protected void updateKits() {
		super.updateKits();

		BingoItems items = new BingoItems();

		DataStorageSerializerRegistry.addSerializer(new ItemStorageSerializer(), SerializableItem.class);

		TagDataAccessor tagData = new TagDataAccessor(server, "data/kits", false);
		tagData.load();

		for (String kitName : tagData.getKeys()) {
			List<SerializableItem> immutableList = tagData.getSerializableList(kitName + ".items", SerializableItem.class);

			List<SerializableItem> itemsCopy = new ArrayList<>(immutableList);
			boolean updated = false;
			int index = 0;
			for (SerializableItem item : immutableList) {
				if (item.stack().compareKey().equals("wand")) {
					itemsCopy.remove(index);
					itemsCopy.add(index, new SerializableItem(item.slot(), items.createStack(GoUpWand.ID)));
					updated = true;
				}

				index++;
			}
			if (updated) {
				tagData.setSerializableList(kitName + ".items", SerializableItem.class, itemsCopy);
			}
		}

		tagData.saveChanges();
	}
}
