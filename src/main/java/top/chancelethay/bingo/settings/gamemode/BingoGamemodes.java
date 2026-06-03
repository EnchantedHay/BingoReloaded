package top.chancelethay.bingo.settings.gamemode;

import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.api.HotswapCardMenu;
import top.chancelethay.bingo.cards.BingoTaskCard;
import top.chancelethay.bingo.cards.BlitzTaskCard;
import top.chancelethay.bingo.cards.CompleteTaskCard;
import top.chancelethay.bingo.cards.HotswapTaskCard;
import top.chancelethay.bingo.cards.LockoutTaskCard;
import top.chancelethay.bingo.cards.TaskCard;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.data.TexturedMenuData;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.gameloop.phase.BingoGame;
import top.chancelethay.bingo.settings.BingoSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class BingoGamemodes {
	public static final Map<String, BingoGamemode> GAMEMODES = new HashMap<>();

	public static BingoGamemode BINGO = register(new BingoGamemode(
			"bingo",
			BingoMessage.MODE_BINGO.asPhrase(),
			TextColor.fromHexString("#309f14"),
			EnumSet.of(GamemodeFeature.UNIQUE_CARD)) {
		@Override
		public TaskCard createTaskCard(CardMenu menu, BingoGame game) {
			return new BingoTaskCard(menu, game.getSettings().size(), game.getProgressTracker());
		}

		@Override
		public Component winScoreText(BingoSettings settings) {
			return Component.text("-----");
		}

		@Override
		public boolean canEndInDraw() {
			return false;
		}

		@Override
		public TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData) {
			return textureData.getTexture("banner_regular");
		}
	});

	public static BingoGamemode LOCKOUT = register(new BingoGamemode(
			"lockout",
			BingoMessage.MODE_LOCKOUT.asPhrase(),
			TextColor.fromHexString("#8138d9"),
			EnumSet.noneOf(GamemodeFeature.class)) {
		@Override
		public TaskCard createTaskCard(CardMenu menu, BingoGame game) {
			return new LockoutTaskCard(menu, game.getSettings().size(), game.getSession(), game.getTeamManager().getActiveTeams(), game.getProgressTracker());
		}

		@Override
		public Component winScoreText(BingoSettings settings) {
			return Component.text(Integer.toString(settings.size().fullCardSize));
		}

		@Override
		public TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData) {
			return textureData.getTexture("banner_lockout");
		}
	});

	public static BingoGamemode COMPLETE = register(new BingoGamemode(
			"complete",
			BingoMessage.MODE_COMPLETE.asPhrase(),
			TextColor.fromHexString("#3d6fe3"),
			EnumSet.of(GamemodeFeature.UNIQUE_CARD, GamemodeFeature.COMPLETE_WIN_GOAL)) {
		@Override
		public TaskCard createTaskCard(CardMenu menu, BingoGame game) {
			return new CompleteTaskCard(menu, game.getSettings().size(), game.getSettings().completeGoal(), game.getProgressTracker());
		}

		@Override
		public Component winScoreText(BingoSettings settings) {
			if (!settings.useScoreAsWinCondition()) {
				return super.winScoreText(settings);
			}

			return Component.text(Integer.toString(settings.completeGoal()));
		}

		@Override
		public TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData) {
			return textureData.getTexture("banner_complete");
		}
	});

	public static BingoGamemode HOTSWAP = register(new BingoGamemode(
			"hotswap",
			BingoMessage.MODE_HOTSWAP.asPhrase(),
			TextColor.fromHexString("#bd2a3f"),
			EnumSet.of(GamemodeFeature.HOTSWAP_WIN_GOAL, GamemodeFeature.TASK_EXPIRATION)) {
		@Override
		public TaskCard createTaskCard(CardMenu menu, BingoGame game) {
			return new HotswapTaskCard((HotswapCardMenu) menu, game.getSettings().size(), game, game.getProgressTracker(), game.getSettings().hotswapGoal(),
					game.getConfig().getOptionValue(BingoOptions.HOTSWAP_CONFIG));
		}

		@Override
		public Component winScoreText(BingoSettings settings) {
			if (!settings.useScoreAsWinCondition()) {
				return super.winScoreText(settings);
			}

			return Component.text(Integer.toString(settings.hotswapGoal()));
		}

		@Override
		public TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData) {
			return textureData.getTexture("banner_hotswap");
		}
	});

	public static BingoGamemode BLITZ = register(new BingoGamemode(
			"blitz",
			BingoMessage.MODE_BLITZ.asPhrase(),
			TextColor.fromHexString("#e5c93e"),
			EnumSet.of(GamemodeFeature.BLITZ_TIMER)) {
		@Override
		public TaskCard createTaskCard(CardMenu menu, BingoGame game) {
			return new BlitzTaskCard(menu, game.getSettings().size(), game, game.getConfig().getOptionValue(BingoOptions.HOTSWAP_CONFIG));
		}

		@Override
		public Component winScoreText(BingoSettings settings) {
			return Component.text("???");
		}

		@Override
		public TexturedMenuData.Texture bannerTexture(TexturedMenuData textureData) {
			return textureData.getTexture("banner_blitz");
		}

		@Override
		public boolean canEndInDraw() {
			return false;
		}
	});

	private static BingoGamemode register(BingoGamemode gamemode) {
		GAMEMODES.put(gamemode.configName(), gamemode);
		return gamemode;
	}

	public static BingoGamemode fromDataString(String data) {
		return fromDataString(data, false);
	}

	public static @Nullable BingoGamemode fromDataString(String data, boolean strict) {
		return GAMEMODES.getOrDefault(data, strict ? null : BingoGamemodes.BINGO);
	}
}
