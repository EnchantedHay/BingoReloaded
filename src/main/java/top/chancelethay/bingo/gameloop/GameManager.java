package top.chancelethay.bingo.gameloop;

import top.chancelethay.bingo.api.BingoEventListener;
import top.chancelethay.bingo.data.BingoLobbyData;
import top.chancelethay.bingo.data.PlayerSerializationData;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.data.helper.SerializablePlayer;
import top.chancelethay.bingo.data.record.GameRecordData;
import top.chancelethay.bingo.data.world.WorldGroup;
import top.chancelethay.bingo.lib.api.BingoReloadedRuntime;
import top.chancelethay.bingo.lib.api.ServerSoftware;
import top.chancelethay.bingo.lib.api.WorldHandle;
import top.chancelethay.bingo.lib.api.WorldPosition;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.event.EventResult;
import top.chancelethay.bingo.lib.util.ComponentUtils;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import top.chancelethay.bingo.lib.util.DebugLogger;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.world.GameWorldManager;
import top.chancelethay.bingo.world.SpawnScatterManager;
import top.chancelethay.bingo.world.Tasks;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the single bingo session and the world lifecycle around it.
 *
 * <p>The plugin runs in a single-world, single-session model: one persistent lobby world (the server
 * world named by {@code defaultWorldName}) and one dedicated game world that is fully regenerated
 * between rounds by {@link GameWorldManager}. There is intentionally no support for multiple
 * concurrent sessions/worlds.
 */
public class GameManager {

	private final BingoReloadedRuntime runtime;
	private final BingoConfigurationData config;

	private final PlayerSerializationData playerData;
	private final BingoEventListener eventListener;
	private final BingoLobbyData lobbyData;
	private final GameRecordData recordData;

	// MineHunt-style world management: a dedicated game world that is regenerated each round, with
	// the configured defaultWorldName acting as the persistent lobby.
	private final Tasks worldTasks;
	private final GameWorldManager gameWorldManager;
	private final SpawnScatterManager scatterManager;
	private final String lobbyWorldName;

	private final Set<UUID> teleportingPlayers;

	// The single bingo session. Null only if the game world could not be set up.
	private @Nullable BingoSession session;
	private boolean sendErrorOnJoin = false;

	public GameManager(@NotNull BingoReloadedRuntime runtime, BingoConfigurationData config) {
		this.runtime = runtime;
		this.config = config;

		this.lobbyData = new BingoLobbyData();
		this.recordData = new GameRecordData();
		this.playerData = new PlayerSerializationData();
		this.teleportingPlayers = new HashSet<>();

		org.bukkit.plugin.Plugin plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(top.chancelethay.bingo.BingoReloadedPaper.class);
		this.worldTasks = new Tasks(plugin);
		this.lobbyWorldName = config.getOptionValue(BingoOptions.DEFAULT_WORLD_NAME);
		this.gameWorldManager = new GameWorldManager(
				worldTasks,
				config.getOptionValue(BingoOptions.WORLD_RESET_GAME_WORLD_NAME),
				!config.getOptionValue(BingoOptions.DISABLE_NETHER),
				!config.getOptionValue(BingoOptions.DISABLE_THE_END),
				config.getOptionValue(BingoOptions.WORLD_RESET_PRELOAD_RADIUS),
				config.getOptionValue(BingoOptions.WORLD_RESET_RANDOM_SEED));
		this.scatterManager = new SpawnScatterManager(worldTasks);

		this.eventListener = new BingoEventListener(this,
				config.getOptionValue(BingoOptions.DISABLE_ADVANCEMENTS),
				config.getOptionValue(BingoOptions.DISABLE_STATISTICS));
	}

	/** Create the game world (if needed), build the single session over it and start pre-generation. */
	public void setup() {
		gameWorldManager.ensureWorlds();

		WorldGroup group = createGameWorldGroup();
		if (group == null) {
			sendErrorOnJoin = true;
			return;
		}

		this.session = new BingoSession(this, group, config);
		gameWorldManager.prepareNextWorlds();
		ConsoleMessenger.log("Bingo session ready in world '" + gameWorldManager.gameWorldName() + "', lobby is '" + lobbyWorldName + "'.");
	}

	private @Nullable WorldGroup createGameWorldGroup() {
		String name = gameWorldManager.gameWorldName();
		WorldHandle overworld = getPlatform().getWorld(name);
		if (overworld == null) {
			ConsoleMessenger.error("Could not build the bingo world group; '" + name + "' does not exist.");
			return null;
		}

		boolean createNether = !config.getOptionValue(BingoOptions.DISABLE_NETHER);
		boolean createEnd = !config.getOptionValue(BingoOptions.DISABLE_THE_END);
		UUID netherId = overworld.uniqueId();
		UUID endId = overworld.uniqueId();

		if (createNether) {
			WorldHandle nether = getPlatform().getWorld(name + "_nether");
			if (nether == null) {
				ConsoleMessenger.error("Could not build the bingo world group; '" + name + "_nether' does not exist.");
				return null;
			}
			netherId = nether.uniqueId();
		}
		if (createEnd) {
			WorldHandle end = getPlatform().getWorld(name + "_the_end");
			if (end == null) {
				ConsoleMessenger.error("Could not build the bingo world group; '" + name + "_the_end' does not exist.");
				return null;
			}
			endId = end.uniqueId();
		}
		return new WorldGroup(getPlatform(), name, overworld.uniqueId(), netherId, endId);
	}

	public @Nullable BingoSession getSession() {
		return session;
	}

	public boolean startGame() {
		if (session == null) {
			ConsoleMessenger.error("Cannot start bingo, the game world is not set up correctly.");
			return false;
		}
		if (session.isRunning()) {
			ConsoleMessenger.log("Could not start bingo because a game is already running.");
			return false;
		}
		session.startGame();
		return true;
	}

	public boolean endGame() {
		if (session == null || !session.isRunning()) {
			ConsoleMessenger.log("Could not end bingo because no game is running.");
			return false;
		}
		session.endGame();
		return true;
	}

	public boolean isRunning() {
		return session != null && session.isRunning();
	}

	public @Nullable BingoSession getSessionFromWorld(@NotNull WorldHandle world) {
		if (session == null) {
			return null;
		}
		if (session.ownsWorld(world)) {
			return session;
		}
		// The lobby world is shared by the session: players waiting there between rounds are still
		// considered part of the session, so they aren't evicted/cleared when sent to the lobby.
		if (world.name().equals(lobbyWorldName)) {
			return session;
		}
		return null;
	}

	public @Nullable BingoSession getSessionOfPlayer(PlayerHandle player) {
		if (session == null) {
			return null;
		}
		return session.teamManager.getPlayerAsParticipant(player) != null ? session : null;
	}

	/**
	 * Regenerate the dedicated game world for the next round: send any players still inside it to the
	 * lobby world, swap in the pre-generated {@code _next} world, refresh the session's world group to
	 * the new world UUIDs, and start pre-generating the world after that.
	 */
	public void regenerateGameWorld(BingoSession session, @Nullable Runnable onComplete) {
		if (gameWorldManager.isResetting()) {
			if (onComplete != null) onComplete.run();
			return;
		}
		session.sendPlayersToLobby();
		// Give teleports a tick to complete before the promote unloads the game world.
		worldTasks.later(() -> gameWorldManager.promoteWhenReady(() -> {
			session.refreshWorldsAfterRegen();
			gameWorldManager.prepareNextWorlds();
			if (onComplete != null) onComplete.run();
		}), 20L);
	}

	public void onPluginDisable() {
		if (session != null) {
			session.destroy();
			session = null;
		}
	}

	public BingoConfigurationData getGameConfig() {
		return config;
	}

	public boolean canPlayerOpenMenus(PlayerHandle player) {
		return getSessionFromWorld(player.world()) != null;
	}

	public EventResult<?> handlePlayerTeleport(final PlayerHandle player, final WorldPosition fromPos, final WorldPosition toPos) {
		WorldHandle sourceWorld = fromPos.world();
		WorldHandle targetWorld = toPos.world();

		// If the world didn't change, the event is not interesting for us
		if (sourceWorld == targetWorld) {
			return EventResult.IGNORE;
		}

		if (teleportingPlayers.contains(player.uniqueId())) {
			teleportingPlayers.remove(player.uniqueId());
			return EventResult.IGNORE;
		}

		if (sourceWorld == null || targetWorld == null) {
			ConsoleMessenger.bug("Source or target world is invalid", this);
			return EventResult.IGNORE;
		}

		BingoSession sourceSession = getSessionFromWorld(sourceWorld);
		BingoSession targetSession = getSessionFromWorld(targetWorld);

		// Same session on both ends (e.g. a portal between game dimensions, or moving to the lobby).
		if (sourceSession == targetSession) {
			return EventResult.IGNORE;
		}

		boolean savePlayerInformation = config.getOptionValue(BingoOptions.SAVE_PLAYER_INFORMATION);

		boolean cancel = false;
		if (sourceSession != null) {
			if (targetSession == null) {
				DebugLogger.addLog("Player leaving the bingo world");
				player.clearInventory(); // If we are leaving a bingo world, we can always clear the player's inventory

				if (savePlayerInformation) {
					teleportingPlayers.add(player.uniqueId());
					// load player will teleport them, so we have to schedule it to make sure to do the right thing
					runtime.getServerSoftware().runTask(t -> {
						if (playerData.loadPlayer(player) == null) {
							ConsoleMessenger.bug(Component.text("No saved player data could be found for ").append(player.displayName()), this);
						}
					});
					cancel = true;
				}
			}
			sourceSession.removePlayer(player);
		}

		if (targetSession != null) {
			if (savePlayerInformation && sourceSession == null) {
				// Only save player data if it does not pertain to a bingo world
				SerializablePlayer serializablePlayer = SerializablePlayer.fromPlayer(runtime.getServerSoftware(), player);
				playerData.savePlayer(serializablePlayer, false);
			}

			// set spawn point of player in session world
			player.setRespawnPoint(targetSession.getOverworld().spawnPoint(), true);
			targetSession.addPlayer(player);
		}

		return new EventResult<>(cancel, null);
	}

	public EventResult<?> handlePlayerJoinsServer(final PlayerHandle player) {
		if (player.hasPermission("bingo.admin") && sendErrorOnJoin) {
			player.sendMessage(ComponentUtils.MINI_BUILDER.deserialize("v(<yellow>" + getPlatform().getExtensionInfo().version() + "</yellow>): <red>Cannot start Bingo Reloaded, something is up with your world setup.</red>\n" +
					"<gray>1.</gray> Make sure the dedicated game world <aqua>" + gameWorldManager.gameWorldName() + "</aqua> can be created (it must not be your main/lobby world)." +
					"\n<gray>2.</gray> If you disabled the nether or the end, reflect this in the config via <aqua>disableNether/disableTheEnd</aqua>."));
		}

		BingoSession targetSession = getSessionFromWorld(player.world());
		if (targetSession != null) {
			targetSession.addPlayer(player);
		}
		return EventResult.IGNORE;
	}

	public EventResult<?> handlePlayerQuitsServer(final PlayerHandle player) {
		BingoSession sourceSession = getSessionFromWorld(player.world());
		if (sourceSession != null) {
			sourceSession.removePlayer(player);
		}
		return EventResult.IGNORE;
	}

	public void prepareNextBingoGame(BingoSession session) {
		if (config.getOptionValue(BingoOptions.SAVE_PLAYER_INFORMATION) &&
				config.getOptionValue(BingoOptions.LOAD_PLAYER_INFORMATION_STRATEGY) == BingoOptions.LoadPlayerInformationStrategy.AFTER_GAME) {
			for (BingoParticipant participant : session.teamManager.getParticipants()) {
				participant.sessionPlayer().ifPresent(player -> {
					session.teamManager.removeMemberFromTeam(participant);
					playerData.loadPlayer(player);
				});
			}
		}
	}

	public PlayerSerializationData getPlayerData() {
		return playerData;
	}

	public ServerSoftware getPlatform() {
		return runtime.getServerSoftware();
	}

	public BingoReloadedRuntime getRuntime() {
		return runtime;
	}

	public BingoEventListener eventListener() {
		return eventListener;
	}

	public BingoLobbyData getLobbyData() {
		return lobbyData;
	}

	public GameRecordData getRecordData() {
		return recordData;
	}

	public GameWorldManager getGameWorldManager() {
		return gameWorldManager;
	}

	public SpawnScatterManager getScatterManager() {
		return scatterManager;
	}

	public Tasks getWorldTasks() {
		return worldTasks;
	}

	public String getLobbyWorldName() {
		return lobbyWorldName;
	}
}
