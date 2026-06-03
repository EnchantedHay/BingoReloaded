package top.chancelethay.bingo.api;

import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.platform.StatisticHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.BingoSettings;
import top.chancelethay.bingo.tasks.GameTask;
import top.chancelethay.bingo.util.timer.CountdownTimer;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Nullable;

public class BingoEvents {

	private BingoEvents() {
	}

	public record PlayerSessionEvent(BingoSession session, PlayerHandle player) {

	}

	public record TaskProgressCompleted(BingoSession session, GameTask task) {

	}

	public record DeathmatchTaskCompleted(BingoSession session, GameTask task) {

	}

	public record TeamParticipantEvent(BingoSession session, BingoParticipant participant, @Nullable BingoTeam team,
								boolean autoTeam) {

	}

	public record GameEnded(BingoSession session, long totalGameTime, @Nullable BingoTeam winningTeam) {

	}

	public record PlaySound(BingoSession session, Sound sound) {

	}

	public record SettingsUpdated(BingoSession session, BingoSettings newSettings) {

	}

	public record StatisticCompleted(BingoSession session, StatisticHandle statistic, BingoParticipant participant) {

	}

	public record CountdownTimerFinished(BingoSession session, CountdownTimer timer) {

	}
}
