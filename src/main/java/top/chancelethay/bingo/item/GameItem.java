package top.chancelethay.bingo.item;

import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.lib.api.item.StackHandle;
import top.chancelethay.bingo.lib.event.EventResult;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.player.BingoParticipant;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public abstract class GameItem implements Keyed {

	private final Key id;

	public GameItem(Key id) {
		this.id = id;
	}

	@Override
	public @NotNull Key key() {
		return id;
	}

	public boolean canLeaveInventory() {
		return false;
	}

	public abstract ItemTemplate defaultTemplate();

	public abstract EventResult<?> use(StackHandle stack, BingoParticipant participant, BingoConfigurationData config);
}
