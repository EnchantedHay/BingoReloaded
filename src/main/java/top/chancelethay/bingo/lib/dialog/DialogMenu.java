package top.chancelethay.bingo.lib.dialog;

import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import top.chancelethay.bingo.BingoReloadedPaper;
import top.chancelethay.bingo.lib.inventory.MenuBoard;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.inventory.Menu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public abstract class DialogMenu implements Menu {

	private final MenuBoard menuBoard;

	public DialogMenu(MenuBoard menuBoard) {
		this.menuBoard = menuBoard;
	}

	public void open(PlayerHandle player) {
		beforeOpening(player);
	}

	@Override
	public MenuBoard getMenuBoard() {
		return menuBoard;
	}

	public abstract Dialog getDialog();

	@Override
	public void beforeOpening(PlayerHandle player) {
		BingoReloadedPaper.showPacketDialog(player, getDialog());
	}

	@Override
	public void beforeClosing(PlayerHandle player) {
	}

	@Override
	public boolean onClick(InventoryClickEvent event, PlayerHandle player, int clickedSlot, ClickType clickType) {
		return true;
	}

	@Override
	public boolean onDrag(InventoryDragEvent event) {
		return true;
	}

	@Override
	public boolean openOnce() {
		return false;
	}

	@Override
	public void setOpenOnce(boolean value) {
	}
}
