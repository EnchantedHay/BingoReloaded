package top.chancelethay.bingo.lib.inventory.action;

import top.chancelethay.bingo.lib.api.item.StackHandlePaper;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.inventory.InventoryMenu;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a container for items that can have MenuActions when clicked.
 */
public class MenuItemGroup
{
    public final Map<Integer, MenuAction> actions;
    public final List<ItemTemplate> items;

    public MenuItemGroup() {
		this.items = new ArrayList<>();
		this.actions = new HashMap<>();
    }

    public boolean handleClick(InventoryMenu menu, final InventoryClickEvent event, PlayerHandle player, int clickedSlot, ClickType clickType) {
        for (ItemTemplate item : new ArrayList<>(items)) {
            int itemSlot = item.getSlot();
            if (itemSlot == clickedSlot) {
                if (actions.containsKey(itemSlot) && actions.get(itemSlot) != null) {
                    actions.get(itemSlot).use(new MenuAction.ActionArguments(menu, player, clickType));
                }
                //TODO: find a way to update itemstack automatically on change, no matter where!
                event.getInventory().setItem(itemSlot, ((StackHandlePaper)item.buildItem()).handle());
            }
        }
        return true;
    }

    public void addItem(ItemTemplate item, @Nullable MenuAction action) {
        items.removeIf(i -> i.getSlot() == item.getSlot());
        items.add(item);
        if (action != null) {
            action.setItem(item);
        }
        actions.put(item.getSlot(), action);
    }

    public void setItemAction(int slot, @NotNull MenuAction action) {
        actions.put(slot, action);
    }

    public void removeItem(int slot) {
        items.removeIf(i -> i.getSlot() == slot);
        actions.remove(slot);
    }
}
