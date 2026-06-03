package top.chancelethay.bingo.gui.inventory.item;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.gui.inventory.TeamCardSelectMenu;
import top.chancelethay.bingo.lib.api.MenuBoard;
import top.chancelethay.bingo.lib.api.item.ItemTypePaper;
import top.chancelethay.bingo.lib.inventory.action.MenuAction;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import org.bukkit.Material;

public class OpenCardSelectAction extends MenuAction {

	private final BingoReloaded bingo;

	public OpenCardSelectAction(BingoReloaded bingo) {
		this.bingo = bingo;
	}

	public static OpenCardSelectAction createItem(BingoReloaded bingo, int slot) {
		OpenCardSelectAction action = new OpenCardSelectAction(bingo);
		action.setItem(new ItemTemplate(ItemTypePaper.of(Material.BUNDLE), BingoReloaded.applyTitleFormat(BingoMessage.SHOW_TEAM_CARD_TITLE.asPhrase())).setSlot(slot));
		return action;
	}

	@Override
	public void use(ActionArguments arguments) {
		// The reason to not use getSessionOfPlayer is that not all players that execute this command have to be active bingo players.
		BingoSession session = bingo.getGameManager().getSessionFromWorld(arguments.player().world());
		if (session == null) {
			return;
		}

		if (!bingo.config().getOptionValue(BingoOptions.ALLOW_VIEWING_ALL_CARDS)) {
			return;
		}

		arguments.menu().setOpenOnce(true);

		MenuBoard board = arguments.menu().getMenuBoard();
		new TeamCardSelectMenu(board, session).open(arguments.player());
	}
}
