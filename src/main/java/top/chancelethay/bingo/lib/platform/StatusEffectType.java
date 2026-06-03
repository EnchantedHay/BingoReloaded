package top.chancelethay.bingo.lib.platform;

import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.intellij.lang.annotations.Subst;

import java.util.Objects;

public final class StatusEffectType {

	private final org.bukkit.potion.PotionEffectType type;

	public StatusEffectType(org.bukkit.potion.PotionEffectType type) {
		this.type = type;
	}

	public static StatusEffectType of(@Subst("minecraft:key") String key) {
		return of(Key.key(key));
	}

	public static StatusEffectType of(Key key) {
		org.bukkit.potion.PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(key);
		if (type == null) {
			return null;
		}
		return new StatusEffectType(type);
	}

	public org.bukkit.potion.PotionEffectType handle() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StatusEffectType other)) {
			return false;
		}
		return type.equals(other.type);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}
}
