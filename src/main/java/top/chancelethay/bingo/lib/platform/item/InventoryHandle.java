package top.chancelethay.bingo.lib.platform.item;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;

public class InventoryHandle {

	private final Inventory inventory;

	public InventoryHandle(Inventory inventory) {
		this.inventory = inventory;
	}

	public Inventory handle() {
		return inventory;
	}

	public void setItem(int index, StackHandle stack) {
		inventory.setItem(index, stack.handle());
	}

	public HashMap<Integer, StackHandle> addItem(StackHandle... stacks) {
		var result = inventory.addItem(Arrays.stream(stacks).map(StackHandle::handle).toArray(ItemStack[]::new));

		HashMap<Integer, StackHandle> returnedHandles = new HashMap<>();
		for (Integer idx : result.keySet()) {
			returnedHandles.put(idx, new StackHandle(result.get(idx)));
		}

		return returnedHandles;
	}

	public @NotNull StackHandle getItem(int index) {
		ItemStack stack = inventory.getItem(index);
		if (stack == null) {
			return StackHandle.create(ItemType.AIR);
		}
		return new StackHandle(stack);
	}

	public void removeItem(StackHandle stack) {
		inventory.remove(stack.handle());
	}

	public StackHandle[] contents() {
		return Arrays.stream(inventory.getContents()).map(StackHandle::new).toArray(StackHandle[]::new);
	}

	public void clearContents() {
		inventory.clear();
	}

	public void setContents(StackHandle[] contents) {
		inventory.setContents(Arrays.stream(contents).map(StackHandle::handle).toArray(ItemStack[]::new));
	}
}
