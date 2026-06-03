package top.chancelethay.bingo.lib.api.item;

import top.chancelethay.bingo.lib.item.ItemTemplate;

public interface StackBuilder {
	StackHandle buildItem(ItemTemplate template, boolean hideAttributes, boolean customTextures);

	default StackHandle buildItem(ItemTemplate template, boolean hideAttributes) {
		return buildItem(template, hideAttributes, false);
	}

	default StackHandle buildItem(ItemTemplate template) {
		return buildItem(template, false, false);
	}
}
