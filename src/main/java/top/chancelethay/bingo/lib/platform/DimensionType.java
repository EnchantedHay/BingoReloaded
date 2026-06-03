package top.chancelethay.bingo.lib.platform;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DimensionType implements Keyed {

	OVERWORLD("overworld"),
	NETHER("nether"),
	THE_END("the_end");

	private final Key key;

	DimensionType(String value) {
		this.key = Key.key("minecraft", value);
	}

	public static @Nullable DimensionType of(Key key) {
		for (DimensionType type : values()) {
			if (type.key.equals(key)) {
				return type;
			}
		}
		return null;
	}

	@Override
	public @NotNull Key key() {
		return key;
	}
}
