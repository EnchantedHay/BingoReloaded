package top.chancelethay.bingo.lib.item;

import top.chancelethay.bingo.lib.api.item.StackHandle;
import org.jetbrains.annotations.NotNull;


public record SerializableItem(int slot, @NotNull StackHandle stack)
{
    public static SerializableItem fromItemTemplate(ItemTemplate template) {
        return new SerializableItem(template.getSlot(), template.buildItem());
    }
}