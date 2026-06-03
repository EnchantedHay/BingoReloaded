package top.chancelethay.bingo.lib.api;

import top.chancelethay.bingo.lib.api.item.ItemType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public interface AdvancementHandle extends Keyed {
	static AdvancementHandle of(Key key) {
		return PlatformResolver.get().resolveAdvancement(key);
	}

	ItemType displayIcon();

	boolean equals(Object other);
}
