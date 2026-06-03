package top.chancelethay.bingo.tasks.tracker;

import top.chancelethay.bingo.gameloop.phase.BingoGame;
import top.chancelethay.bingo.lib.api.StatisticHandle;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.BingoPlayer;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.tasks.data.StatisticTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StatisticTracker
{
    private final List<StatisticProgress> statistics;

    public StatisticTracker()
    {
        this.statistics = new ArrayList<>();
    }

    public double getProgressLeft(BingoPlayer player, StatisticHandle statistic)
    {
        List<StatisticProgress> statProgress = statistics.stream().filter(progress ->
                progress.getParticipant().equals(player) && progress.getStatistic().equals(statistic)).toList();

        if (statProgress.size() != 1)
            return Double.MAX_VALUE;

        return statProgress.getFirst().getProgressLeft();
    }

    public void addStatistic(StatisticTask statTask, BingoParticipant participant, Consumer<StatisticProgress> completedCallback) {
        if (statistics.stream().anyMatch(progress ->
                progress.getParticipant().equals(participant) && progress.getStatistic().equals(statTask.statistic())))
            return;

        setPlayerStatistic(statTask.statistic(), participant, 0);
        statistics.add(new StatisticProgress(statTask.statistic(), participant, statTask.count(), completedCallback));
    }

    public void removeStatistic(StatisticTask task) {
        statistics.removeIf(progress -> progress.getStatistic().equals(task.statistic()));
    }

    public void updateProgress()
    {
        statistics.forEach(StatisticProgress::updatePeriodicProgress);
        statistics.removeIf(StatisticProgress::done);
    }

    public void reset()
    {
        statistics.clear();
    }

    public void handleStatisticIncrement(@NotNull BingoParticipant player, StatisticHandle statistic, int newValue, final BingoGame game)
    {
        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        List<StatisticProgress> matchingStatistic = statistics.stream().filter(progress ->
                progress.getParticipant().equals(player) && progress.getStatistic().equals(statistic)).toList();
        if (matchingStatistic.size() == 1)
        {
            matchingStatistic.getFirst().setProgress(newValue);
        }

        statistics.removeIf(StatisticProgress::done);
    }

    public void setPlayerStatistic(StatisticHandle statistic, BingoParticipant player, int value)
    {
        if (player.sessionPlayer().isEmpty())
            return;

        PlayerHandle gamePlayer = player.sessionPlayer().get();

        if (statistic.hasItemType())
        {
            gamePlayer.setStatisticValue(statistic.statisticType(), statistic.itemType(), value);
        }
        else if (statistic.hasEntity())
        {
            gamePlayer.setStatisticValue(statistic.statisticType(), statistic.entityType(), value);
        }
        else
        {
            gamePlayer.setStatisticValue(statistic.statisticType(), value);
        }
    }
}
