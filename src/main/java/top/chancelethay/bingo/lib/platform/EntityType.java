package top.chancelethay.bingo.lib.platform;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class EntityType implements Keyed {

	private final org.bukkit.entity.EntityType type;

	public EntityType(org.bukkit.entity.EntityType type) {
		this.type = type;
	}

	public static EntityType of(Key key) {
		org.bukkit.entity.EntityType type = Registry.ENTITY_TYPE.get(key);
		if (type == null) {
			return null;
		}
		return new EntityType(type);
	}

	@Override
	public @NotNull Key key() {
		return type.key();
	}

	public org.bukkit.entity.EntityType handle() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityType other) {
			return type.equals(other.type);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}
}
