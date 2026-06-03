package top.chancelethay.bingo.lib.api.item;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public interface InventoryHandle {

	void setItem(int index, StackHandle stack);
	HashMap<Integer, StackHandle> addItem(StackHandle... stacks);
	@NotNull StackHandle getItem(int index);
	void removeItem(StackHandle stack);

	StackHandle[] contents();
	void clearContents();
	void setContents(StackHandle[] contents);
}
