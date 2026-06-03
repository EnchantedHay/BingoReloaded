package top.chancelethay.bingo.lib.platform.player;

import top.chancelethay.bingo.lib.platform.item.InventoryHandle;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import org.bukkit.inventory.PlayerInventory;

public final class PlayerInventoryHandle extends InventoryHandle {

	private final PlayerInventory inventory;

	public PlayerInventoryHandle(PlayerInventory inventory) {
		super(inventory);
		this.inventory = inventory;
	}

	public StackHandle mainHandItem() {
		return new StackHandle(inventory.getItemInMainHand());
	}

	public StackHandle offHandItem() {
		return new StackHandle(inventory.getItemInOffHand());
	}

	@Override
	public PlayerInventory handle() {
		return inventory;
	}
}
