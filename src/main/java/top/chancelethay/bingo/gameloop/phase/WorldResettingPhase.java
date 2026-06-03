package top.chancelethay.bingo.gameloop.phase;

import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.platform.InteractAction;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.events.EventResult;
import top.chancelethay.bingo.settings.BingoSettings;
import top.chancelethay.bingo.world.GameWorldManager;
import top.chancelethay.bingo.world.Tasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

/**
 * Explicit "world is being regenerated" phase, modeled after MineHunt's {@code RESETTING} state.
 *
 * <p>Entered after a game finishes: players have been sent to the lobby world and the dedicated game
 * world is being regenerated (pre-generated {@code _next} world swapped in) in the background. This
 * phase simply holds the session in a non-playable state and shows regeneration progress to the
 * waiting players. {@link BingoSession#beginWorldReset()} replaces it with a {@link PregameLobby}
 * once the fresh world is ready, so this phase has no completion logic of its own.
 */
public class WorldResettingPhase implements GamePhase
{
    private final BingoSession session;
    private final GameWorldManager worldManager;
    private final Tasks tasks;
    private @Nullable BukkitTask progressTask;

    public WorldResettingPhase(BingoSession session, GameWorldManager worldManager) {
        this.session = session;
        this.worldManager = worldManager;
        this.tasks = session.getGameManager().getWorldTasks();
    }

    @Override
    public @Nullable BingoSession getSession() {
        return session;
    }

    @Override
    public void setup() {
        showProgress();
        progressTask = tasks.repeat(this::showProgress, 20L, 20L);
    }

    private void showProgress() {
        int pct = worldManager.getNextProgressPercent();
        Component message = Component.text("Generating the next world... " + pct + "%", NamedTextColor.AQUA);
        for (PlayerHandle player : session.getSessionPlayers()) {
            player.sendActionBar(message);
        }
    }

    @Override
    public void end() {
        tasks.cancel(progressTask);
        progressTask = null;
    }

    @Override
    public void handlePlayerJoinedSessionWorld(PlayerHandle player) {
        showProgress();
    }

    @Override
    public void handlePlayerLeftSessionWorld(PlayerHandle player) {
    }

    @Override
    public void handleSettingsUpdated(BingoSettings newSettings) {
    }

    @Override
    public EventResult<?> handlePlayerInteracted(PlayerHandle player, @Nullable StackHandle stack, InteractAction action) {
        return EventResult.IGNORE;
    }

    @Override
    public boolean canViewCard() {
        return false;
    }
}
