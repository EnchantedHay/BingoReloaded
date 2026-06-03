package top.chancelethay.bingo.cards;


import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.gamemode.BingoGamemode;
import top.chancelethay.bingo.tasks.GameTask;
import top.chancelethay.bingo.tasks.TaskGenerator;
import top.chancelethay.bingo.tasks.tracker.TaskProgressTracker;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public abstract class TaskCard
{
    public final CardSize size;
    private final List<GameTask> tasks;
    private final TaskProgressTracker progressTracker;

    protected final CardMenu menu;

    public TaskCard(CardMenu menu, CardSize size, TaskProgressTracker progressTracker)
    {
        this.size = size;
        this.tasks = new ArrayList<>();
        this.menu = menu;
        this.progressTracker = progressTracker;
        menu.setInfo(BingoMessage.INFO_REGULAR_NAME.asPhrase(),
                BingoMessage.INFO_REGULAR_DESC.asMultiline());
    }

    public abstract BingoGamemode getMode();
    public abstract boolean hasTeamWon(BingoTeam team);
    public abstract TaskCard copy(@Nullable Component alternateTitle);
    public abstract boolean canGenerateSeparateCards();

    /**
     * @param settings settings to use for card generation.
     */
    public void generateCard(TaskGenerator.GeneratorSettings settings) {
        setTasks(TaskGenerator.generateCardTasks(settings));
    }

    public void showInventory(PlayerHandle player) {
        menu.updateTasks(getTasks());
        menu.open(player);
    }

    public List<GameTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<GameTask> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
        this.menu.updateTasks(tasks);
    }

    /**
     * @param team The team.
     * @return The amount of completed items for the given team.
     */
    public int getCompleteCount(@NotNull BingoTeam team) {
        int count = 0;
        for (var task : getTasks()) {
            if (task.isCompletedByTeam(team))
                count++;
        }

        return count;
    }

    public int getCompleteCount(@NotNull BingoParticipant participant) {
        return (int) getTasks().stream()
                .filter(t -> t.getCompletedByPlayer().isPresent() && t.getCompletedByPlayer().get().getId().equals(participant.getId())).count();
    }

    TaskProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public void onTaskCompleted(BingoParticipant player, GameTask task, long timeSeconds) {}

}
