package top.chancelethay.bingo.lib.events;

import top.chancelethay.bingo.lib.platform.AdvancementHandle;
import top.chancelethay.bingo.lib.platform.InteractAction;
import top.chancelethay.bingo.lib.platform.PaperApiHelper;
import top.chancelethay.bingo.lib.platform.StatisticHandle;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EventListenerPaper implements Listener {

	private final JavaPlugin plugin;
	private final PlatformEventDispatcher dispatcher;

	public EventListenerPaper(JavaPlugin plugin, PlatformEventDispatcher dispatcher) {
		this.plugin = plugin;
		this.dispatcher = dispatcher;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void handlePlayerMoveEvent(final PlayerMoveEvent event) {
		EventResult<?> result = dispatcher.sendPlayerMove(new PlayerHandle(event.getPlayer()),
				PaperApiHelper.worldPosFromLocation(event.getFrom()),
				PaperApiHelper.worldPosFromLocation(event.getTo()));

		if (!result.consume())
			return;

		Location newLoc = event.getTo();
        newLoc.setX(event.getFrom().getX());
        newLoc.setZ(event.getFrom().getZ());
        event.setTo(newLoc);
	}

	@EventHandler
	public void handlePlayerDeathEvent(final PlayerDeathEvent event) {
		List<? extends StackHandle> drops = event.getDrops().stream()
				.map(StackHandle::new)
				.toList();
		EventResult<EventResults.PlayerDeathResult> result = dispatcher.sendPlayerDeath(new PlayerHandle(event.getPlayer()), drops);

		event.getDrops().clear();

		if (result.data() == null || !result.data().keepInventory()) {
			event.getDrops().addAll(drops.stream()
					.map(s -> s.handle())
					.toList());
		} else {
			event.setKeepInventory(true);
		}

		event.setCancelled(result.consume());
	}

	@EventHandler
	public void handlePlayerRespawnEvent(final PlayerRespawnEvent event) {
		EventResult<EventResults.PlayerRespawnResult> result = dispatcher.sendPlayerRespawn(
				new PlayerHandle(event.getPlayer()),
				event.isBedSpawn(),
				event.isAnchorSpawn());

		var data = result.data();
		if (data != null && data.overwriteSpawnPoint()) {
			if (data.newSpawnPoint() == null) {
				ConsoleMessenger.bug("New spawnpoint cannot be null when respawing player!", this);
				return;
			}
			event.setRespawnLocation(PaperApiHelper.locationFromWorldPos(data.newSpawnPoint()));
		}
	}

	// We need the game manager to handle us first to make sure no player information gets lost by accident.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerTeleportEvent(final PlayerTeleportEvent event) {
		EventResult<?> result = dispatcher.sendPlayerTeleport(
				new PlayerHandle(event.getPlayer()),
				PaperApiHelper.worldPosFromLocation(event.getFrom()),
				PaperApiHelper.worldPosFromLocation(event.getTo()));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerPortalEvent(final PlayerPortalEvent event) {
		EventResult<EventResults.PlayerMoveResult> result = dispatcher.sendPlayerPortal(
				new PlayerHandle(event.getPlayer()),
				PaperApiHelper.worldPosFromLocation(event.getFrom()),
				PaperApiHelper.worldPosFromLocation(event.getTo()));

		if (result.consume()) {
			event.setCancelled(true);
		}

		if (result.data() == null) {
			return;
		}

		if (!result.data().overwritePosition() || result.data().newPosition() == null) {
			return;
		}

		event.setTo(PaperApiHelper.locationFromWorldPos(result.data().newPosition()));
	}

	@EventHandler
	public void handlePlayerDroppedStackEvent(final PlayerDropItemEvent event) {
		EventResult<?> result = dispatcher.sendPlayerDroppedStack(
				new PlayerHandle(event.getPlayer()),
				new StackHandle(event.getItemDrop().getItemStack()));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerItemDamaged(final PlayerItemDamageEvent event) {
		EventResult<?> result = dispatcher.sendPlayerStackDamaged(
				new PlayerHandle(event.getPlayer()),
				new StackHandle(event.getItem()));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerInteracted(final PlayerInteractEvent event) {

		if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
			return;
		}

		EventResult<?> result = dispatcher.sendPlayerInteracted(
				new PlayerHandle(event.getPlayer()),
				new StackHandle(event.getItem()),
				new InteractAction(event.getAction().isLeftClick(), event.getAction().isRightClick(), event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerDamageEvent(final EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
			return;
		}

		EventResult<?> result = dispatcher.sendPlayerFallDamage(new PlayerHandle(player));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerJoinEvent(final PlayerJoinEvent event) {
		dispatcher.sendPlayerJoinsServer(new PlayerHandle(event.getPlayer()));
	}

	@EventHandler
	public void handlePlayerQuitEvent(final PlayerQuitEvent event) {
		dispatcher.sendPlayerQuitsServer(new PlayerHandle(event.getPlayer()));
	}

	@EventHandler
	public void handlePlayerBreakBlockEvent(final BlockBreakEvent event) {
		EventResult<?> result = dispatcher.sendPlayerBreaksBlock(
				new PlayerHandle(event.getPlayer()),
				PaperApiHelper.worldPosFromLocation(event.getBlock().getLocation()),
				ItemType.of(event.getBlock().getType()));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerPlaceBlockEvent(final BlockPlaceEvent event) {
		EventResult<?> result = dispatcher.sendPlayerPlacesBlock(
				new PlayerHandle(event.getPlayer()),
				PaperApiHelper.worldPosFromLocation(event.getBlock().getLocation()),
				ItemType.of(event.getBlock().getType()));

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerStatisticIncrementEvent(final PlayerStatisticIncrementEvent event) {
		EventResult<?> result = dispatcher.sendPlayerStatisticIncrement(
				new PlayerHandle(event.getPlayer()),
				StatisticHandle.create(event.getStatistic(), event.getEntityType(), event.getMaterial()),
				event.getNewValue());

		if (result.consume()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePlayerAdvancementEvent(final PlayerAdvancementDoneEvent event) {
		dispatcher.sendPlayerAdvancementDone(
				new PlayerHandle(event.getPlayer()),
				new AdvancementHandle(event.getAdvancement()));
	}

	@EventHandler
	public void handlePlayerPickupItemEvent(final EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		EventResult<EventResults.PlayerPickupResult> result = dispatcher.sendPlayerPickupStack(
				new PlayerHandle(player),
				new StackHandle(event.getItem().getItemStack()),
				PaperApiHelper.worldPosFromLocation(event.getItem().getLocation()));

		if (result.consume()) {
			event.setCancelled(true);
		}

		if (result.data() == null) {
			return;
		}

		if (result.data().removeItem()) {
			event.getItem().getItemStack().setAmount(0);
			return;
		}

		if (result.data().overwriteItem() && result.data().newItem() != null) {
			event.getItem().setItemStack(result.data().newItem().handle());
		}
	}

	@EventHandler
	public void handlePlayerInventoryClick(final InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		EventResult<?> result = dispatcher.sendPlayerInventoryClick(
				new PlayerHandle(player),
				new StackHandle(event.getWhoClicked().getItemOnCursor()),
				event.getSlotType() == InventoryType.SlotType.RESULT,
				event.isShiftClick());

		if (result.consume()) {
			event.setCancelled(true);
		}
	}
}
