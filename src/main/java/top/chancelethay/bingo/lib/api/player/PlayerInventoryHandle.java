package top.chancelethay.bingo.lib.api.player;

import top.chancelethay.bingo.lib.api.item.InventoryHandle;
import top.chancelethay.bingo.lib.api.item.StackHandle;

public interface PlayerInventoryHandle extends InventoryHandle {

	StackHandle mainHandItem();
	StackHandle offHandItem();
}
