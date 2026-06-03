package top.chancelethay.bingo.item;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.lib.api.item.ItemType;
import top.chancelethay.bingo.lib.api.item.StackHandle;
import top.chancelethay.bingo.lib.event.EventResult;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.player.BingoParticipant;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class TeamShulker extends GameItem {

	public static Key ID = BingoReloaded.resourceKey("team_shulker");

	public TeamShulker() {
		super(ID);
	}

	@Override
	public ItemTemplate defaultTemplate() {
		return new ItemTemplate(ItemType.of(Key.key("red_shulker_box"))).setDummy(true);
	}

	@Override
	public EventResult<?> use(StackHandle stack, BingoParticipant participant, BingoConfigurationData config) {
		participant.sendMessage(Component.text("You clicked on the team shulker :D"));
		return EventResult.CONSUME;
	}
}
