package top.chancelethay.bingo.cards;

import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.gamemode.BingoGamemode;
import top.chancelethay.bingo.settings.gamemode.BingoGamemodes;
import top.chancelethay.bingo.tasks.GameTask;
import top.chancelethay.bingo.tasks.tracker.TaskProgressTracker;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompleteTaskCard extends TaskCard {

	private final int completeGoal;

	public CompleteTaskCard(@NotNull CardMenu menu, CardSize size, int completeGoal, TaskProgressTracker progressTracker) {
		super(menu, size, progressTracker);
		menu.setInfo(BingoMessage.INFO_COMPLETE_NAME.asPhrase(),
				BingoMessage.INFO_COMPLETE_DESC.asMultiline());
		this.completeGoal = completeGoal;
	}

	@Override
	public BingoGamemode getMode() {
		return BingoGamemodes.COMPLETE;
	}

	@Override
	public boolean hasTeamWon(@NotNull BingoTeam team) {
		return getCompleteCount(team) == Math.min(completeGoal, size.fullCardSize);
	}

	@Override
	public TaskCard copy(@Nullable Component alternateTitle) {
		CompleteTaskCard card = new CompleteTaskCard(menu.copy(alternateTitle), this.size, this.completeGoal, getProgressTracker());
		List<GameTask> newTasks = new ArrayList<>();
		for (var t : getTasks()) {
			newTasks.add(t.copy());
		}
		card.setTasks(newTasks);
		return card;
	}

	@Override
	public boolean canGenerateSeparateCards() {
		return true;
	}
}
