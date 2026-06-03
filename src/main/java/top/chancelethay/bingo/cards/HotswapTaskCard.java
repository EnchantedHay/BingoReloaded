package top.chancelethay.bingo.cards;

import top.chancelethay.bingo.api.HotswapCardMenu;
import top.chancelethay.bingo.cards.hotswap.ExpiringHotswapTask;
import top.chancelethay.bingo.cards.hotswap.HotswapTaskSlot;
import top.chancelethay.bingo.cards.hotswap.SimpleHotswapTask;
import top.chancelethay.bingo.data.BingoCardData;
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
import top.chancelethay.bingo.tasks.tracker.TaskProgressTracker;
import top.chancelethay.bingo.util.CollectionHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class HotswapTaskCard extends TaskCard
{
    private final int winningScore;

    private final Random randomExpiryProvider;
    private final int minExpirationTime;
    private final int maxExpirationTime;
    private final int recoveryTimeSeconds;
    private final boolean showExpirationAsDurability;
    private final boolean expireTasksAutomatically;

    private final List<HotswapTaskSlot> taskHolders;
    private Supplier<GameTask> bingoTaskGenerator;

    private final List<GameTask> completedTasks;
    private final BingoCardData cardData;
    // Used to call the play sound event for expiring tasks
    private final BingoGame game;

    // List used to draw random tasks from
    private RotatingTaskList randomTasks = null;

    public HotswapTaskCard(@NotNull HotswapCardMenu menu, CardSize size, BingoGame game, TaskProgressTracker progressTracker, int winningScore, BingoConfigurationData.HotswapConfig config) {
        super(menu, size, progressTracker);

        this.randomExpiryProvider = new Random();
        this.taskHolders = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.bingoTaskGenerator = () -> null;
        this.cardData = new BingoCardData();
        this.game = game;
        this.minExpirationTime = config.minimumExpiration();
        this.maxExpirationTime = config.maximumExpiration();
        this.recoveryTimeSeconds = config.recoveryTime();
        this.showExpirationAsDurability = config.showExpirationAsDurability() && game.getSettings().expireHotswapTasks();
        this.expireTasksAutomatically = game.getSettings().expireHotswapTasks();

        game.getTimer().addNotifier(this::updateTaskExpiration);

        this.winningScore = game.getSettings().useScoreAsWinCondition() ? winningScore : -1;

        Component[] description = new Component[]{};
        if (this.expireTasksAutomatically) {
            description = BingoMessage.INFO_HOTSWAP_DESC_EXPIRE.asMultiline(Component.text(game.getSettings().countdownDuration()));
        }
        Component[] extraDescription = switch (game.getSettings().countdownType()) {
            case DISABLED -> BingoMessage.INFO_HOTSWAP_DESC_SCORE.asMultiline(Component.text(game.getSettings().hotswapGoal()));
            case DURATION -> BingoMessage.INFO_HOTSWAP_DESC_TIME.asMultiline();
            case TIME_LIMIT -> BingoMessage.INFO_HOTSWAP_DESC_ANY.asMultiline(Component.text(game.getSettings().hotswapGoal()));
        };
        menu.setInfo(BingoMessage.INFO_HOTSWAP_NAME.asPhrase(), CollectionHelper.concatWithArrayCopy(description, extraDescription));
    }

    @Override
    public BingoGamemode getMode() {
        return BingoGamemodes.HOTSWAP;
    }

    @Override
    public boolean hasTeamWon(@NotNull BingoTeam team) {
        if (winningScore == -1) {
            return false;
        }
        return getCompleteCount(team) >= winningScore;
    }

    // Hotswap cards cannot be copied since it should be the same instance for every player.
    @Override
    public TaskCard copy(@Nullable Component alternateTitle) {
        return this;
    }

    @Override
    public boolean canGenerateSeparateCards() {
        return false;
    }

    /**
     * Overridden to set up the task generator
     */
    @Override
    public void generateCard(TaskGenerator.GeneratorSettings settings) {
        super.generateCard(settings);

        if (settings.seed() != 0) {
            randomExpiryProvider.setSeed(settings.seed());
        }

        this.randomTasks = new RotatingTaskList(settings, settings.seed());
    }

    @Override
    public void setTasks(List<GameTask> tasks) {
        taskHolders.clear();
        if (expireTasksAutomatically) {
            for (GameTask task : tasks) {
                int expirationTime = randomExpiryProvider.nextInt(minExpirationTime * 60, (maxExpirationTime * 60) + 1);
                taskHolders.add(new ExpiringHotswapTask(expirationTime, recoveryTimeSeconds, showExpirationAsDurability));
            }
        } else {
            for (GameTask task : tasks) {
                taskHolders.add(new SimpleHotswapTask(recoveryTimeSeconds));
            }
        }

        ((HotswapCardMenu)menu).updateTaskHolders(taskHolders);
        super.setTasks(tasks);
    }

    @Override
    public void onTaskCompleted(BingoParticipant player, GameTask task, long timeSeconds) {
        super.onTaskCompleted(player, task, timeSeconds);
        completedTasks.add(task);
    }

    public void updateTaskExpiration(long timeElapsed) {
        int idx = 0;
        int taskExpiredCount = 0;
        int taskRecoveredCount = 0;
        GameTask lastExpiredTask = null;
        GameTask lastRecoverdTask = null;

        for (HotswapTaskSlot holder : taskHolders) {
            GameTask task = getTasks().get(idx);
            if (!holder.isRecovering() && task.isCompleted()) { // start recovering item when it's been completed
                holder.startRecovering();
                idx++;
                continue;
            }

            holder.updateTaskTime();

            if (holder.getCurrentTime() <= 0) {
                if (holder.isRecovering()) {
                    taskRecoveredCount++;
                    // Recovery finished, replace task with a new one.
                    GameTask newTask = randomTasks.nextTask(this::canTaskBeAdded);
                    if (newTask == null) {
                        ConsoleMessenger.bug("Cannot generate new task for hot-swap", this);
                    }
                    lastRecoverdTask = newTask;

                    if (expireTasksAutomatically) {
                        int expirationTime = randomExpiryProvider.nextInt(minExpirationTime, (maxExpirationTime + 1)) * 60;
                        taskHolders.set(idx, new ExpiringHotswapTask(expirationTime, recoveryTimeSeconds, showExpirationAsDurability));
                    } else {
                        taskHolders.set(idx, new SimpleHotswapTask(recoveryTimeSeconds));
                    }
                    getTasks().set(idx, newTask);
                    getProgressTracker().startTrackingTask(newTask);
                } else {
                    taskExpiredCount++;
                    lastExpiredTask = task;
                    task.setVoided(true);
                    holder.startRecovering();
                    getProgressTracker().removeTask(task);
                }
            }
            idx++;
        }

        if (taskExpiredCount > 0) {
            game.playSound(BingoSound.HOTSWAP_TASK_EXPIRED.builder().build());

            if (taskExpiredCount == 1) {
                GameTask taskToSend = lastExpiredTask;
                game.getActionBar().requestMessage(p ->
                                Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_SINGLE_EXPIRED.asPhrase(taskToSend.data.getName()).color(TextColor.fromHexString("#e85e21"))).build(),
                        1, 3);
            }
            else {
                game.getActionBar().requestMessage(p -> Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_MULTIPLE_EXPIRED.asPhrase().color(TextColor.fromHexString("#e85e21"))).build(),
                        1, 3);
            }
        }
        if (taskRecoveredCount > 0) {
            game.playSound(BingoSound.HOTSWAP_TASK_ADDED.builder().build());

            if (taskRecoveredCount == 1) {
                GameTask taskToSend = lastRecoverdTask;
                game.getActionBar().requestMessage(p ->
                                Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_SINGLE_ADDED.asPhrase(taskToSend.data.getName()).color(TextColor.fromHexString("#5cb1ff"))).build(),
                        2, 3);
            }
            else {
                game.getActionBar().requestMessage(p -> Component.text().decorate(TextDecoration.BOLD).append(BingoMessage.HOTSWAP_MULTIPLE_ADDED.asPhrase().color(TextColor.fromHexString("#5cb1ff"))).build(),
                        1, 3);
            }
        }

        menu.updateTasks(getTasks());
		((HotswapCardMenu)menu).updateTaskHolders(taskHolders);
    }

    @Override
    public int getCompleteCount(@NotNull BingoTeam team) {
        return (int) completedTasks.stream().filter(task -> task.isCompletedByTeam(team)).count();
    }

    @Override
    public int getCompleteCount(@NotNull BingoParticipant participant) {
        return (int) completedTasks.stream()
                .filter(t -> t.getCompletedByPlayer().isPresent() && t.getCompletedByPlayer().get().getId().equals(participant.getId())).count();
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
