package top.chancelethay.bingo.lib.platform;

import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.util.DebugLogger;
import org.bukkit.World;

import java.util.Collection;
import java.util.UUID;

public final class WorldHandle {

	private final World world;

	public WorldHandle(World world) {
		this.world = world;
	}

	public String name() {
		return world.getName();
	}

	public UUID uniqueId() {
		return world.getUID();
	}

	public Collection<? extends PlayerHandle> players() {
		return world.getPlayers().stream().map(PlayerHandle::new).toList();
	}

	public WorldPosition spawnPoint() {
		DebugLogger.addLog("Spawn point of " + name() + ": " + world.getSpawnLocation());
		return PaperApiHelper.worldPosFromLocation(world.getSpawnLocation());
	}

	public DimensionType dimension() {
		return switch (world.getEnvironment()) {
			case NETHER -> DimensionType.NETHER;
			case THE_END -> DimensionType.THE_END;
			default -> DimensionType.OVERWORLD;
		};
	}

	public void spawnEntity(EntityType type, WorldPosition pos) {
		world.spawnEntity(PaperApiHelper.locationFromWorldPos(pos), type.handle());
	}

	public void setStorming(boolean storm) {
		world.setStorm(storm);
	}

	public void setTimeOfDay(long time) {
		world.setTime(time);
	}

	public ItemType typeAtPos(WorldPosition pos) {
		return ItemType.of(world.getType(pos.blockX(), pos.blockY(), pos.blockZ()).key());
	}

	public void setTypeAtPos(WorldPosition pos, ItemType type) {
		world.setType(PaperApiHelper.locationFromWorldPos(pos), type.handle());
	}

	public WorldPosition highestBlockAt(WorldPosition pos) {
		return PaperApiHelper.worldPosFromLocation(world.getHighestBlockAt(pos.blockX(), pos.blockZ()).getLocation());
	}

	public void dropItem(StackHandle item, WorldPosition location) {
		world.dropItem(PaperApiHelper.locationFromWorldPos(location), item.handle());
	}

	public World handle() {
		return world;
	}
}
