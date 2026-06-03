package top.chancelethay.bingo.api;

import top.chancelethay.bingo.cards.CardSize;
import top.chancelethay.bingo.settings.gamemode.BingoGamemode;
import top.chancelethay.bingo.settings.gamemode.BingoGamemodes;

public record CardDisplayInfo(BingoGamemode mode,
							  CardSize size,
							  TaskDisplayMode advancementDisplay,
							  TaskDisplayMode statisticDisplay,
							  boolean allowViewingOtherCards) {

	public static final CardDisplayInfo DUMMY_DISPLAY_INFO = new CardDisplayInfo(
			BingoGamemodes.BINGO,
			CardSize.X5,
			TaskDisplayMode.UNIQUE_TASK_ITEMS,
			TaskDisplayMode.UNIQUE_TASK_ITEMS,
			false);
}
