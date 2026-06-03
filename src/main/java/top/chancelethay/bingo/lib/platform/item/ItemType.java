package top.chancelethay.bingo.lib.platform.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ItemType implements Keyed {

	public static final ItemType AIR = of(Key.key("air"));

	private final Material type;

	public ItemType(@Nullable Material type) {
		this.type = type == null ? Material.AIR : type;
	}

	public static ItemType of(@NotNull @Subst("minecraft:resource") String type) {
		return of(Key.key(type));
	}

	public static ItemType of(@NotNull Key type) {
		return new ItemType(Registry.MATERIAL.get(type));
	}

	public static ItemType of(Material type) {
		return new ItemType(type);
	}

	public boolean isBlock() {
		return type.isBlock();
	}

	public boolean isAir() {
		return type == Material.AIR;
	}

	public boolean isSolid() {
		return type.isSolid();
	}

	@Override
	public @NotNull Key key() {
		return type.key();
	}

	public Material handle() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemType other) {
			return type.equals(other.type);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}
}
