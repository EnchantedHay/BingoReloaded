package top.chancelethay.bingo.world;

import top.chancelethay.bingo.data.BingoLobby;
import top.chancelethay.bingo.data.BingoLobbyData;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.lib.platform.PaperApiHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Lobby world protection, adapted from the MineHunt project's {@code LobbyListener}.
 *
 * <p>The bingo game always runs in the dedicated, regenerated {@code worldReset} world, so the
 * persistent lobby world (the world named by {@link BingoOptions#DEFAULT_WORLD_NAME}) is never an
 * active game world. This listener turns that lobby world into a safe, calm "waiting area" while
 * players wait between rounds:
 * <ul>
 *   <li>locks the world to a clear midday (no day/night or weather cycle);</li>
 *   <li>stops natural mob, wandering trader and patrol spawns;</li>
 *   <li>cancels fall, void, PVP and hunger damage;</li>
 *   <li>teleports players back to the lobby spawn if they fall into the void.</li>
 * </ul>
 *
 * <p>Every behaviour is individually toggleable under {@code lobbyProtection} in config.yml and is
 * read live, so {@code /bingo reload} takes effect immediately for the event-driven rules. The
 * world-wide gamerule/time lock is (re)applied whenever {@link #applyLobbyRules()} runs.
 */
public final class LobbyProtectionListener implements Listener {

	private static final int VOID_FALL_Y = -10;

	private final JavaPlugin plugin;
	private final BingoConfigurationData config;
	private final BingoLobbyData lobbyData;
	private final String lobbyWorldName;

	private World lobbyWorld;

	public LobbyProtectionListener(JavaPlugin plugin, BingoConfigurationData config) {
		this.plugin = plugin;
		this.config = config;
		this.lobbyData = new BingoLobbyData();
		this.lobbyWorldName = config.getOptionValue(BingoOptions.DEFAULT_WORLD_NAME);
	}

	public void enable() {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		this.lobbyWorld = Bukkit.getWorld(lobbyWorldName);
		applyLobbyRules();
	}

	/* ========================================================================================
	 * Lobby detection and world rules
	 * ======================================================================================== */

	private boolean protectionEnabled() {
		return config.getOptionValue(BingoOptions.LOBBY_PROTECTION_ENABLED);
	}

	private boolean isLobby(World world) {
		if (world == null) {
			return false;
		}
		if (lobbyWorld != null) {
			return world == lobbyWorld;
		}
		// The lobby world may not have been loaded yet when this listener was enabled; resolve lazily.
		if (world.getName().equals(lobbyWorldName)) {
			lobbyWorld = world;
			return true;
		}
		return false;
	}

	/** Lock the lobby world to a calm midday "showroom" state. Safe to call repeatedly. */
	public void applyLobbyRules() {
		if (!protectionEnabled()) {
			return;
		}
		World world = (lobbyWorld != null) ? lobbyWorld : Bukkit.getWorld(lobbyWorldName);
		if (world == null) {
			return;
		}
		this.lobbyWorld = world;

		try {
			if (config.getOptionValue(BingoOptions.LOBBY_LOCK_TIME_AND_WEATHER)) {
				world.setGameRule(GameRules.ADVANCE_TIME, false);
				world.setTime(6000L);
				world.setGameRule(GameRules.ADVANCE_WEATHER, false);
				world.setStorm(false);
				world.setThundering(false);
			}
			if (config.getOptionValue(BingoOptions.LOBBY_PREVENT_MOB_SPAWNING)) {
				world.setGameRule(GameRules.SPAWN_MOBS, false);
				world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
				world.setGameRule(GameRules.SPAWN_PATROLS, false);
				world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
				world.setGameRule(GameRules.MOB_GRIEFING, false);
			}
		} catch (Throwable ignored) {
			// Defensive: a gamerule may be missing on an unexpected server version. Never block enable.
		}
	}

	/* ========================================================================================
	 * Per-player protection
	 * ======================================================================================== */

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_PREVENT_DAMAGE)) {
			return;
		}
		if (!isLobby(player.getWorld())) {
			return;
		}

		if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			event.setCancelled(true);
			return;
		}
		if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
			event.setCancelled(true);
			if (config.getOptionValue(BingoOptions.LOBBY_RETURN_ON_VOID)) {
				teleportToLobbySpawn(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyPVP(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player target)) {
			return;
		}
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_PREVENT_DAMAGE)) {
			return;
		}
		if (isLobby(target.getWorld())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyHunger(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_PREVENT_DAMAGE)) {
			return;
		}
		if (!isLobby(player.getWorld())) {
			return;
		}
		event.setCancelled(true);
		player.setFoodLevel(20);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyMobSpawn(CreatureSpawnEvent event) {
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_PREVENT_MOB_SPAWNING)) {
			return;
		}
		if (!isLobby(event.getLocation().getWorld())) {
			return;
		}
		// Allow plugin/custom spawns (e.g. decorative lobby entities); block only natural ones.
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyWeather(WeatherChangeEvent event) {
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_LOCK_TIME_AND_WEATHER)) {
			return;
		}
		if (!isLobby(event.getWorld())) {
			return;
		}
		// Only block the transition into rain/storm; clearing weather is always allowed.
		if (event.toWeatherState()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onLobbyVoidFall(PlayerMoveEvent event) {
		if (event.getTo() == null || event.getTo().getBlockY() >= VOID_FALL_Y) {
			return;
		}
		if (!protectionEnabled() || !config.getOptionValue(BingoOptions.LOBBY_RETURN_ON_VOID)) {
			return;
		}
		Player player = event.getPlayer();
		if (!isLobby(player.getWorld())) {
			return;
		}
		teleportToLobbySpawn(player);
	}

	private void teleportToLobbySpawn(Player player) {
		Location spawn = resolveLobbySpawn();
		if (spawn == null) {
			return;
		}
		player.teleport(spawn);
		player.setFallDistance(0);
	}

	/** The configured {@code /bingo lobby} spawn if set, otherwise the lobby world's natural spawn. */
	private Location resolveLobbySpawn() {
		BingoLobby lobby = lobbyData.getCreatedLobby();
		if (lobby != null && lobby.spawnPosition() != null) {
			return PaperApiHelper.locationFromWorldPos(lobby.spawnPosition());
		}
		World world = (lobbyWorld != null) ? lobbyWorld : Bukkit.getWorld(lobbyWorldName);
		return world != null ? world.getSpawnLocation() : null;
	}
}
