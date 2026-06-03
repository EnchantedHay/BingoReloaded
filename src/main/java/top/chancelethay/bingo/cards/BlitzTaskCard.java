package top.chancelethay.bingo.cards;

import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.api.HotswapCardMenu;
import top.chancelethay.bingo.cards.hotswap.HotswapTaskSlot;
import top.chancelethay.bingo.cards.hotswap.SimpleHotswapTask;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.data.BingoSound;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.gameloop.phase.BingoGame;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.gamemode.BingoGamemode;
import top.chancelethay.bingo.settings.gamemode.BingoGamemodes;
import top.chancelethay.bingo.tasks.GameTask;
import top.chancelethay.bingo.tasks.RotatingTaskList;
import top.chancelethay.bingo.tasks.TaskGenerator;
import top.chancelethay.bingo.tasks.data.TaskData;
import top.chancelethay.bingo.util.timer.BlitzTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class BlitzTaskCard extends TaskCard {

	private final List<HotswapTaskSlot> slots = new ArrayList<>();

	private final int recoveryTimeSeconds;
	private final int resetBufferAmount;
	private final List<GameTask> completedTasks = new ArrayList<>();
	private RotatingTaskList randomTasks = null;
	private final BingoGame game;

	private final Queue<GameTask> taskResetBuffer = new ArrayDeque<>();

	public BlitzTaskCard(CardMenu menu, CardSize size, BingoGame game, BingoConfigurationData.HotswapConfig config) {
		super(menu, size, game.getProgressTracker());
		this.game = game;

		game.getTimer().addNotifier(this::updateWithTime);
		recoveryTimeSeconds = config.recoveryTime();
		resetBufferAmount = 4;

		menu.setInfo(BingoMessage.INFO_BLITZ_NAME.asPhrase(),
				BingoMessage.INFO_BLITZ_DESC.asMultiline());
	}

	@Override
	public BingoGamemode getMode() {
		return BingoGamemodes.BLITZ;
	}

	@Override
	public boolean hasTeamWon(BingoTeam team) {
		return false;
	}

	// Cannot be copied because there is only 1 team.
	@Override
	public TaskCard copy(@Nullable Component alternateTitle) {
		return this;
	}

	@Override
	public boolean canGenerateSeparateCards() {
		return false;
	}

	@Override
	public void generateCard(TaskGenerator.GeneratorSettings settings) {
		super.generateCard(settings);

		this.randomTasks = new RotatingTaskList(settings, settings.seed());
	}

	private void updateWithTime(long newTime) {
		if (newTime == 0) {
			// Game is lost!
		}

		int amountRecovered = 0;
		GameTask lastRecoveredTask = null;

		int idx = 0;
		for (HotswapTaskSlot slot : slots) {
			GameTask task = getTasks().get(idx);

			// Check if this task is the next task to be reset, if the buffer is full.
			if (taskResetBuffer.peek() == task && taskResetBuffer.size() >= resetBufferAmount) {
				if (!slot.isRecovering() && task.isCompleted()) {

					taskResetBuffer.remove();
					slot.startRecovering();
					idx++;
					continue;
				}
				else if (slot.isRecovering()) {
					// Don't keep recovering tasks in the buffer.
					taskResetBuffer.remove();
				}
			}

			slot.updateTaskTime();

			if (slot.getCurrentTime() <= 0) {
				if (slot.isRecovering()) {
					amountRecovered++;
					// Recovery finished, replace task with a new one.
					GameTask newTask = randomTasks.nextTask(this::canTaskBeAdded);
					if (newTask == null) {
						ConsoleMessenger.bug("Cannot generate new task for blitz", this);
					}
					lastRecoveredTask = newTask;
					slots.set(idx, new SimpleHotswapTask(recoveryTimeSeconds));
					getTasks().set(idx, newTask);
					getProgressTracker().startTrackingTask(newTask);
				}
			}
			idx++;
		}

		if (amountRecovered > 0) {
			game.playSound(BingoSound.HOTSWAP_TASK_ADDED.builder().build());

			if (amountRecovered == 1) {
				GameTask taskToSend = lastRecoveredTask;
				game.getActionBar().requestMessage(p ->
								Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_SINGLE_ADDED.asPhrase(taskToSend.data.getName()).color(TextColor.fromHexString("#5cb1ff"))).build(),
						2, 3);
			} else {
				game.getActionBar().requestMessage(p -> Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_MULTIPLE_ADDED.asPhrase().color(TextColor.fromHexString("#5cb1ff"))).build(),
						1, 3);
			}
		}

		menu.updateTasks(getTasks());
		((HotswapCardMenu) menu).updateTaskHolders(slots);
	}

	@Override
	public void setTasks(List<GameTask> tasks) {
		slots.clear();
		for (GameTask task : tasks) {
			slots.add(new SimpleHotswapTask(recoveryTimeSeconds));
		}

		((HotswapCardMenu)menu).updateTaskHolders(slots);
		super.setTasks(tasks);
	}

	@Override
	public int getCompleteCount(@NotNull BingoTeam team) {
		return (int) completedTasks.stream().filter(t -> t.isCompletedByTeam(team)).count();
	}

	@Override
	public int getCompleteCount(@NotNull BingoParticipant participant) {
		return (int) completedTasks.stream()
				.filter(t -> t.getCompletedByPlayer().isPresent() && t.getCompletedByPlayer().get().getId().equals(participant.getId())).count();
	}

	@Override
	public void onTaskCompleted(BingoParticipant player, GameTask task, long timeSeconds) {
		super.onTaskCompleted(player, task, timeSeconds);
		completedTasks.add(task);
		taskResetBuffer.add(task);
		if (game.getTimer() instanceof BlitzTimer timer) {
			timer.reset();
		}
	}

	private boolean canTaskBeAdded(TaskData data) {
		for (GameTask task : getTasks()) {
			if (task.data.isTaskEqual(data)) {
				return true;
			}
		}
		return false;
	}
}
