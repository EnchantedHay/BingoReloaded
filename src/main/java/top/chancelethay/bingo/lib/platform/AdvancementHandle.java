package top.chancelethay.bingo.lib.platform;

import top.chancelethay.bingo.lib.platform.item.ItemType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdvancementHandle implements Keyed {

	private final Advancement advancement;

	public AdvancementHandle(Advancement advancement) {
		this.advancement = advancement;
	}

	public static AdvancementHandle of(Key key) {
		return new AdvancementHandle(Bukkit.getAdvancement(new NamespacedKey(key.namespace(), key.value())));
	}

	public ItemType displayIcon() {
		if (advancement.getDisplay() == null) {
			return ItemType.AIR;
		}
		return new ItemType(advancement.getDisplay().icon().getType());
	}

	@Override
	public @NotNull Key key() {
		return advancement.key();
	}

	public Advancement handle() {
		return advancement;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AdvancementHandle other) {
			return key().equals(other.key());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(advancement);
	}
}
