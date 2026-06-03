package top.chancelethay.bingo.gameloop.phase;

import top.chancelethay.bingo.api.BingoEvents;
import top.chancelethay.bingo.gameloop.SessionMember;
import top.chancelethay.bingo.lib.api.InteractAction;
import top.chancelethay.bingo.lib.api.item.StackHandle;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.event.EventResult;
import top.chancelethay.bingo.settings.BingoSettings;
import org.jetbrains.annotations.Nullable;

public interface GamePhase extends SessionMember
{
    /**
     * To be called when this phase needs to (forcefully) end.
     */
    void end();
    void handlePlayerJoinedSessionWorld(PlayerHandle player);
    void handlePlayerLeftSessionWorld(PlayerHandle player);
    void handleSettingsUpdated(final BingoSettings newSettings);
    EventResult<?> handlePlayerInteracted(PlayerHandle player, @Nullable StackHandle stack, InteractAction action);

    default void handleParticipantJoinedTeam(final BingoEvents.TeamParticipantEvent event) {};
    default void handleParticipantLeftTeam(final BingoEvents.TeamParticipantEvent event) {};

	boolean canViewCard();
}
