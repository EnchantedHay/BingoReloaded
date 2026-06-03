package top.chancelethay.bingo.gameloop.phase;

import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.api.InteractAction;
import top.chancelethay.bingo.lib.api.item.StackHandle;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.event.EventResult;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.BingoSettings;
import top.chancelethay.bingo.settings.PlayerKit;
import top.chancelethay.bingo.util.timer.CountdownTimer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class PostGamePhase implements GamePhase
{
    private final CountdownTimer timer;
    private final BingoSession session;

    public PostGamePhase(BingoSession session, int durationSeconds) {
        this.session = session;
        this.timer = new CountdownTimer(durationSeconds, this::onTimerFinished);
    }

    @Override
    public @Nullable BingoSession getSession() {
        return session;
    }

    @Override
    public void setup() {
        if (timer.getStartTime() <= 0) {
            session.beginWorldReset();
            timer.stop();
            return;
        }
        timer.start();
        timer.addNotifier(this::onTimerTicks);
        sendRestartMessage(timer.getTime(), session);
    }

    @Override
    public void end() {
		// Players may still have the card GUI open even though the card does not exist anymore
		for (PlayerHandle p : session.getPlayersInWorld()) {
			p.closeInventory();
		}

        for (BingoTeam team : session.teamManager.getActiveTeams()) {
            team.setCard(null);
        }
    }

    @Override
    public void handlePlayerJoinedSessionWorld(PlayerHandle player) {
        sendRestartMessage(this.timer.getTime(), player);
    }

    @Override
    public void handlePlayerLeftSessionWorld(PlayerHandle player) {

    }

    @Override
    public void handleSettingsUpdated(BingoSettings newSettings) {

    }

    @Override
    public EventResult<?> handlePlayerInteracted(PlayerHandle player, @Nullable StackHandle stack, InteractAction action) {
        BingoParticipant participant = session.teamManager.getPlayerAsParticipant(player);
        if (participant == null || participant.sessionPlayer().isEmpty())
            return EventResult.IGNORE;

        if (stack == null || stack.type().isAir())
            return EventResult.IGNORE;

        if (!action.rightClick())
            return EventResult.IGNORE;

        if (PlayerKit.CARD_ITEM.isCompareKeyEqual(stack)) {
            // Show bingo card to player
            participant.showCard(null);
            return EventResult.CONSUME;
        }
        return EventResult.IGNORE;
    }

	@Override
	public boolean canViewCard() {
		return true;
	}

	private void onTimerTicks(long timeLeft) {
        if (timeLeft == 5) {
            sendRestartMessage(timeLeft, session);
        }
    }

    private void onTimerFinished() {
        session.beginWorldReset();
        timer.stop();
    }

    public void sendRestartMessage(long timeLeft, Audience audience) {
        BingoMessage.POST_GAME_START.sendToAudience(audience, NamedTextColor.RED, Component.text(timeLeft).color(NamedTextColor.BLUE));
    }
}
