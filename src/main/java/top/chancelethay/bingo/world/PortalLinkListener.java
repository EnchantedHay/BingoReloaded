package top.chancelethay.bingo.world;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.gameloop.GameManager;
import top.chancelethay.bingo.lib.platform.WorldHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * Entity portal linking for the dedicated bingo game world group.
 *
 * <p>Adapted from the MineHunt project's {@code PortalLinkListener}. BingoReloaded already redirects
 * <em>player</em> portal travel to the session's correct custom dimension (see
 * {@link BingoSession#handlePlayerPortalEvent}). This listener covers the remaining case: non-player
 * entities (mobs walking/pushed through nether portals, minecarts, dropped items, etc.).
 *
 * <p>Without it, those entities fall back to vanilla/Paper portal linking and can leak into the
 * server's main nether/end instead of arriving in the session's freshly regenerated
 * {@code <gameWorld>_nether} / {@code <gameWorld>_the_end}. The target dimension is taken from the
 * live session's {@link top.chancelethay.bingo.data.world.WorldGroup}, so it stays correct across the
 * per-round world regeneration (which changes the underlying world UUIDs).
 *
 * <p>It also handles the one player case that {@link BingoSession#handlePlayerPortalEvent} cannot:
 * when a player enters the freshly generated session End via an end portal, vanilla does <em>not</em>
 * build the obsidian entry platform in a portal destination that was redirected to a custom world, so
 * the player would drop straight into the void. {@link #ensureEndEntryPlatform} lays that platform
 * down before the teleport completes. This mirrors the same safeguard in MineHunt.
 */
public final class PortalLinkListener implements Listener {

	// Nether is 1/8 the scale of the overworld; nether y is clamped below the bedrock ceiling.
	private static final double NETHER_SCALE = 8.0;
	private static final double NETHER_MIN_Y = 5.0;
	private static final double NETHER_MAX_Y = 118.0;
	private static final double OVERWORLD_MIN_Y = 5.0;
	private static final double OVERWORLD_MAX_Y = 246.0;

	// Vanilla's fixed end-entry obsidian platform location (centre). Players redirected into the
	// session End land around (100, 50, 0), i.e. on top of this platform.
	private static final int END_PLATFORM_X = 100;
	private static final int END_PLATFORM_Y = 48;
	private static final int END_PLATFORM_Z = 0;

	private final JavaPlugin plugin;
	private final BingoReloaded bingo;

	public PortalLinkListener(JavaPlugin plugin, BingoReloaded bingo) {
		this.plugin = plugin;
		this.bingo = bingo;
	}

	public void enable() {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Guarantee a safe landing when a player enters the session End. Runs only when leaving the
	 * session overworld through an end portal; it does not modify the teleport itself (the world
	 * redirect is owned by {@link BingoSession#handlePlayerPortalEvent}), it just makes sure the
	 * obsidian platform exists at the destination first.
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if (event.getCause() != TeleportCause.END_PORTAL) {
			return;
		}
		Location from = event.getFrom();
		if (from == null || from.getWorld() == null
				|| from.getWorld().getEnvironment() != World.Environment.NORMAL) {
			return;
		}

		GameManager manager = bingo.getGameManager();
		if (manager == null) {
			return;
		}
		BingoSession session = manager.getSession();
		if (session == null) {
			return;
		}
		WorldHandle fromHandle = manager.getPlatform().getWorld(from.getWorld().getUID());
		if (fromHandle == null || !session.ownsWorld(fromHandle)) {
			return;
		}

		ensureEndEntryPlatform(toBukkitWorld(session.getEndWorld()));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent event) {
		Location from = event.getFrom();
		if (from == null || from.getWorld() == null) {
			return;
		}

		GameManager manager = bingo.getGameManager();
		if (manager == null) {
			return;
		}
		BingoSession session = manager.getSession();
		if (session == null) {
			return;
		}

		World fromWorld = from.getWorld();
		WorldHandle fromHandle = manager.getPlatform().getWorld(fromWorld.getUID());
		if (fromHandle == null || !session.ownsWorld(fromHandle)) {
			// Not one of the session's worlds; leave vanilla behaviour untouched.
			return;
		}

		Location target = computeTarget(session, fromWorld, from);
		if (target == null) {
			// The target dimension is unavailable (e.g. disabled). Cancel rather than let the entity
			// leak into a vanilla/main-server world.
			event.setCancelled(true);
			return;
		}

		event.setTo(target);
		event.setSearchRadius(fromWorld.getEnvironment() == World.Environment.NETHER ? 128 : 16);
	}

	/**
	 * Mirror of {@link BingoSession#handlePlayerPortalEvent} for entities: overworld&harr;nether use the
	 * 8:1 coordinate scaling, end&rarr;overworld returns to the overworld spawn. Coordinates are computed
	 * here (rather than trusting the event's vanilla {@code to}) because the entity target can be null
	 * or point at the wrong, non-session dimension.
	 */
	private @Nullable Location computeTarget(BingoSession session, World fromWorld, Location from) {
		switch (fromWorld.getEnvironment()) {
			case NORMAL -> {
				World nether = toBukkitWorld(session.getNetherWorld());
				if (nether == null) {
					return null;
				}
				double y = clamp(from.getY(), NETHER_MIN_Y, NETHER_MAX_Y);
				return new Location(nether, from.getX() / NETHER_SCALE, y, from.getZ() / NETHER_SCALE);
			}
			case NETHER -> {
				World overworld = toBukkitWorld(session.getOverworld());
				if (overworld == null) {
					return null;
				}
				double y = clamp(from.getY(), OVERWORLD_MIN_Y, OVERWORLD_MAX_Y);
				return new Location(overworld, from.getX() * NETHER_SCALE, y, from.getZ() * NETHER_SCALE);
			}
			case THE_END -> {
				World overworld = toBukkitWorld(session.getOverworld());
				return overworld != null ? overworld.getSpawnLocation() : null;
			}
			default -> {
				return null;
			}
		}
	}

	/**
	 * Lay a 5x5 obsidian platform with head clearance at the canonical end-entry location, mirroring
	 * MineHunt. Idempotent: if the platform already exists this is a single cheap block check.
	 */
	private void ensureEndEntryPlatform(@Nullable World endWorld) {
		if (endWorld == null) {
			return;
		}
		if (endWorld.getBlockAt(END_PLATFORM_X, END_PLATFORM_Y, END_PLATFORM_Z).getType() == Material.OBSIDIAN) {
			return;
		}

		for (int x = END_PLATFORM_X - 2; x <= END_PLATFORM_X + 2; x++) {
			for (int z = END_PLATFORM_Z - 2; z <= END_PLATFORM_Z + 2; z++) {
				endWorld.getBlockAt(x, END_PLATFORM_Y, z).setType(Material.OBSIDIAN, false);
			}
		}
		// Clear the space above the platform so the player isn't suffocated/blocked on arrival.
		for (int y = END_PLATFORM_Y + 1; y <= END_PLATFORM_Y + 4; y++) {
			for (int x = END_PLATFORM_X - 2; x <= END_PLATFORM_X + 2; x++) {
				for (int z = END_PLATFORM_Z - 2; z <= END_PLATFORM_Z + 2; z++) {
					Block block = endWorld.getBlockAt(x, y, z);
					if (block.getType().isSolid()) {
						block.setType(Material.AIR, false);
					}
				}
			}
		}
	}

	private static @Nullable World toBukkitWorld(@Nullable WorldHandle handle) {
		return handle == null ? null : handle.handle();
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
