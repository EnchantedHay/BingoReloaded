package top.chancelethay.bingo.data;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.lib.data.core.DataAccessor;
import top.chancelethay.bingo.lib.item.SerializableItem;
import top.chancelethay.bingo.settings.PlayerKit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultKitData {
	private final DataAccessor data = BingoReloaded.getDataAccessor("data/default_kits");

	public record Kit(List<SerializableItem> items) {
	}

	public @Nullable Kit getKit(PlayerKit slot)
	{
		return data.getSerializable(slot.configName, Kit.class);
	}
}
