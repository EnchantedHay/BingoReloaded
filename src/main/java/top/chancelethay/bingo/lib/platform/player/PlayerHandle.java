package top.chancelethay.bingo.lib.platform.player;

import top.chancelethay.bingo.lib.platform.ActionUser;
import top.chancelethay.bingo.lib.platform.AdvancementHandle;
import top.chancelethay.bingo.lib.platform.EntityType;
import top.chancelethay.bingo.lib.platform.PaperApiHelper;
import top.chancelethay.bingo.lib.platform.PlayerGamemode;
import top.chancelethay.bingo.lib.platform.PotionEffectInstance;
import top.chancelethay.bingo.lib.platform.StatisticType;
import top.chancelethay.bingo.lib.platform.WorldHandle;
import top.chancelethay.bingo.lib.platform.WorldPosition;
import top.chancelethay.bingo.lib.platform.item.InventoryHandle;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.util.DebugLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.Waypoint;
import org.bukkit.GameMode;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class PlayerHandle implements ForwardingAudience, ActionUser {

	private final Player player;

	public PlayerHandle(@NotNull Player player) {
		this.player = player;
	}

	public String playerName() {
		return player.getName();
	}

	public Component displayName() {
		return player.displayName();
	}

	public UUID uniqueId() {
		return player.getUniqueId();
	}

	public WorldHandle world() {
		return new WorldHandle(player.getWorld());
	}

	public WorldPosition position() {
		return PaperApiHelper.worldPosFromLocation(player.getLocation());
	}

	public @Nullable WorldPosition respawnPoint() {
		return PaperApiHelper.worldPosFromLocation(player.getRespawnLocation());
	}

	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}

	public int level() {
		return player.getLevel();
	}

	public float exp() {
		return player.getExp();
	}

	public double health() {
		return player.getHealth();
	}

	public int foodLevel() {
		return player.getFoodLevel();
	}

	public PlayerGamemode gamemode() {
		return toPlayerMode(player.getGameMode());
	}

	public int getStatisticValue(StatisticType type) {
		return player.getStatistic(type.handle());
	}

	public int getStatisticValue(StatisticType type, EntityType entity) {
		return player.getStatistic(type.handle(), entity.handle());
	}

	public int getStatisticValue(StatisticType type, ItemType item) {
		return player.getStatistic(type.handle(), item.handle());
	}

	public void teleportAsync(WorldPosition pos, @Nullable Consumer<Boolean> whenFinished) {
		DebugLogger.addLog("Teleporting player async to pos: " + pos.x() + ", " + pos.y() + ", " + pos.z() + ", world: " + pos.world().name());

		var future = player.teleportAsync(PaperApiHelper.locationFromWorldPos(pos), PlayerTeleportEvent.TeleportCause.PLUGIN);
		if (whenFinished != null) {
			future.thenAccept(whenFinished);
		}
	}

	public void teleportAsync(WorldPosition pos) {
		teleportAsync(pos, null);
	}

	/**
	 * Blocking teleport is way faster compared to teleportAsync if the chunk is already loaded, Else it is way slower.
	 *
	 * @return true when the teleport was successful.
	 */
	public boolean teleportBlocking(WorldPosition pos) {
		DebugLogger.addLog("Teleporting player blocking to pos: " + pos.x() + ", " + pos.y() + ", " + pos.z() + ", world: " + pos.world().name());
		return player.teleport(PaperApiHelper.locationFromWorldPos(pos), PlayerTeleportEvent.TeleportCause.PLUGIN);
	}

	public PlayerInventoryHandle inventory() {
		return new PlayerInventoryHandle(player.getInventory());
	}

	public void clearInventory() {
		player.getInventory().clear();
	}

	public void openInventory(InventoryHandle inventory) {
		player.openInventory(inventory.handle());
	}

	public InventoryHandle enderChest() {
		return new InventoryHandle(player.getEnderChest());
	}

	/**
	 * @param newSpawn new position.
	 * @param force    true if setting the spawn point should ignore valid bed/respawn positions too.
	 */
	public void setRespawnPoint(WorldPosition newSpawn, boolean force) {
		player.setRespawnLocation(PaperApiHelper.locationFromWorldPos(newSpawn), force);
	}

	public void setLevel(int level) {
		player.setLevel(level);
	}

	public void setExp(float exp) {
		player.setExp(exp);
	}

	public void setFoodLevel(int foodLevel) {
		player.setFoodLevel(foodLevel);
	}

	public void setHealth(double health) {
		player.setHealth(health);
	}

	public void setGamemode(PlayerGamemode gamemode) {
		player.setGameMode(fromPlayerMode(gamemode));
	}

	public void setStatisticValue(StatisticType type, int value) {
		player.setStatistic(type.handle(), value);
	}

	public void setStatisticValue(StatisticType type, EntityType entity, int value) {
		player.setStatistic(type.handle(), entity.handle(), value);
	}

	public void setStatisticValue(StatisticType type, ItemType item, int value) {
		player.setStatistic(type.handle(), item.handle(), value);
	}

	public void addEffect(PotionEffectInstance effect) {
		player.addPotionEffect(new PotionEffect(
				effect.effect().handle(),
				effect.durationTicks(),
				effect.amplifier(),
				effect.ambient(),
				effect.particles(),
				effect.icon()));
	}

	public static GameMode fromPlayerMode(PlayerGamemode gamemode) {
		return switch (gamemode) {
			case SPECTATOR -> GameMode.SPECTATOR;
			case CREATIVE -> GameMode.CREATIVE;
			case SURVIVAL -> GameMode.SURVIVAL;
			case ADVENTURE -> GameMode.ADVENTURE;
		};
	}

	public static PlayerGamemode toPlayerMode(GameMode gameMode) {
		return switch (gameMode) {
			case SURVIVAL -> PlayerGamemode.SURVIVAL;
			case CREATIVE -> PlayerGamemode.CREATIVE;
			case SPECTATOR -> PlayerGamemode.SPECTATOR;
			case ADVENTURE -> PlayerGamemode.ADVENTURE;
		};
	}

	public void clearAllEffects() {
		player.clearActivePotionEffects();
	}

	public void removeAdvancementProgress(AdvancementHandle advancement) {
		AdvancementProgress progress = player.getAdvancementProgress(advancement.handle());
		progress.getAwardedCriteria().forEach(progress::revokeCriteria);
	}

	public boolean hasCooldown(StackHandle stack) {
		return player.hasCooldown(stack.handle());
	}

	public boolean hasCooldownOnGroup(Key cooldownGroup) {
		return player.getCooldown(cooldownGroup) > 0;
	}

	public void setCooldown(StackHandle stack, int cooldownTicks) {
		player.setCooldown(stack.handle(), cooldownTicks);
	}

	public void setCooldownOnGroup(Key cooldownGroup, int cooldownTicks) {
		player.setCooldown(cooldownGroup, cooldownTicks);
	}

	public boolean isSneaking() {
		return player.isSneaking();
	}

	public void closeInventory() {
		player.closeInventory();
	}

	public void kick(@Nullable Component reason) {
		player.kick(reason, PlayerKickEvent.Cause.PLUGIN);
	}

	public void setWaypointColor(@Nullable TextColor color) {
		ServerPlayer player = ((CraftPlayer)handle()).getHandle();
		Waypoint.Icon icon = player.waypointIcon();
		icon.color = color == null ? Optional.empty() : Optional.of(color.value());
		icon.cloneAndAssignStyle(player);
		player.level().getWaypointManager().addPlayer(player);
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return List.of(player);
	}

	public Player handle() {
		return player;
	}
}
