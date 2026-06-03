package top.chancelethay.bingo.lib.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerPacketEvent extends Event {

	private final UUID userId;

	private static final HandlerList HANDLERS = new HandlerList();

	public PlayerPacketEvent(UUID userId) {
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}

	@NotNull
	@Override
	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}
}