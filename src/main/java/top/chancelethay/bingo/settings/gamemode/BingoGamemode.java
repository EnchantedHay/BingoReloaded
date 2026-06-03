package top.chancelethay.bingo.settings.gamemode;

import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.cards.TaskCard;
import top.chancelethay.bingo.data.TexturedMenuData;
import top.chancelethay.bingo.gameloop.phase.BingoGame;
import top.chancelethay.bingo.settings.BingoSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public abstract class BingoGamemode implements ComponentLike {

	private final Component displayName;
	private final TextColor color;
	private final String configName;
	private final EnumSet<GamemodeFeature> featureSet;

	BingoGamemode(String configName, Component displayName, TextColor color, EnumSet<GamemodeFeature> featureSet) {
		this.configName = configName;
		this.displayName = displayName;
		this.color = color;
		this.featureSet = featureSet;
	}

	@Override
	public @NotNull Component asComponent() {
		return displayName.color(color);
	}

	public TextColor getColor() {
		return color;
	}

	public String configName() {
		return configName;
	}

	public abstract TaskCard createTaskCard(CardMenu menu, BingoGame game);

	public abstract TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData);

	public Component winScoreText(BingoSettings settings) {
		return Component.text("-");
	}

	public boolean canEndInDraw() {
		return true;
	}

	public EnumSet<GamemodeFeature> featureSet() {
		return featureSet;
	}
}
