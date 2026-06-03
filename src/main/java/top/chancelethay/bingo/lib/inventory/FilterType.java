package top.chancelethay.bingo.lib.inventory;

import top.chancelethay.bingo.lib.util.StringAdditions;

public enum FilterType
{
    NONE,
    ITEM_ID,
    DISPLAY_NAME,
    MATERIAL,
    CUSTOM;

    @Override
    public String toString() {
        return StringAdditions.capitalize(name());
    }
}
