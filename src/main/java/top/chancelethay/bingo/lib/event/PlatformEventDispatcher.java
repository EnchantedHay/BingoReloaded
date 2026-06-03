package top.chancelethay.bingo.lib.event;

import top.chancelethay.bingo.lib.api.AdvancementHandle;
import top.chancelethay.bingo.lib.api.InteractAction;
import top.chancelethay.bingo.lib.api.StatisticHandle;
import top.chancelethay.bingo.lib.api.WorldPosition;
import top.chancelethay.bingo.lib.api.item.ItemType;
import top.chancelethay.bingo.lib.api.item.StackHandle;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents the events that are expected to be sent by the target platform/software
 */
public interface PlatformEventDispatcher {

	EventResult<?> sendPlayerMove(PlayerHandle player, WorldPosition from, WorldPosition to);
	EventResult<?> sendPlayerTeleport(PlayerHandle player, WorldPosition from, WorldPosition to);
	EventResult<EventResults.PlayerMoveResult> sendPlayerPortal(PlayerHandle player, WorldPosition from, WorldPosition to);
	EventResult<?> sendPlayerDroppedStack(PlayerHandle player, StackHandle item);
	EventResult<?> sendPlayerStackDamaged(PlayerHandle player, StackHandle item);
	EventResult<?> sendPlayerInteracted(PlayerHandle player, @Nullable StackHandle handItem, InteractAction action);
	EventResult<?> sendPlayerFallDamage(PlayerHandle player);
	EventResult<EventResults.PlayerDeathResult> sendPlayerDeath(PlayerHandle player, Collection<? extends StackHandle> drops);
	EventResult<EventResults.PlayerRespawnResult> sendPlayerRespawn(PlayerHandle player, boolean isBedSpawn, boolean isAnchorSpawn);
	EventResult<?> sendPlayerJoinsServer(PlayerHandle player);
	EventResult<?> sendPlayerQuitsServer(PlayerHandle player);
	EventResult<?> sendPlayerBreaksBlock(PlayerHandle player, WorldPosition position, ItemType blockType);
	EventResult<?> sendPlayerPlacesBlock(PlayerHandle player, WorldPosition position, ItemType blockType);
	EventResult<?> sendPlayerStatisticIncrement(PlayerHandle player, StatisticHandle statistic, int newValue);
	EventResult<?> sendPlayerAdvancementDone(PlayerHandle player, AdvancementHandle advancement);
	EventResult<EventResults.PlayerPickupResult> sendPlayerPickupStack(PlayerHandle player, StackHandle stack, WorldPosition itemLocation);
	EventResult<?> sendPlayerInventoryClick(PlayerHandle player, StackHandle itemOnCursor, boolean resultSlot, boolean shiftClick);
}
