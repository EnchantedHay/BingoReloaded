package top.chancelethay.bingo.lib.inventory;

import top.chancelethay.bingo.lib.platform.ServerSoftware;
import top.chancelethay.bingo.lib.platform.item.InventoryHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;


public interface InventoryMenu extends Menu {
	@NotNull
	Inventory getInventory();

	default void openInventory(PlayerHandle player) {
		ServerSoftware.get().runTask(task -> player.openInventory(new InventoryHandle(getInventory())));
	}

	static Component inputButtonText(Component buttonText) {
		return Component.text()
				.append(Component.text("<").color(NamedTextColor.DARK_GRAY))
				.append(buttonText.color(NamedTextColor.GRAY))
				.append(Component.text(">").color(NamedTextColor.DARK_GRAY))
				.append(Component.text(": ").color(NamedTextColor.WHITE))
				.build();
	}

	Component INPUT_LEFT_CLICK = inputButtonText(Component.keybind("key.attack"));
	Component INPUT_RIGHT_CLICK = inputButtonText(Component.keybind("key.use"));
	// tutorial.punch_tree.description resolves to "Hold down %1" in English.
	Component INPUT_SHIFT_CLICK = inputButtonText(Component.translatable("tutorial.punch_tree.description", Component.translatable("key.keyboard.left.shift")));
}
