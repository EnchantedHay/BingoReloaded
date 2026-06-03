package top.chancelethay.bingo.lib.events;

import java.util.UUID;

public class PlayerDisplayAnvilTextChangedEvent extends PlayerPacketEvent {

	private final String newText;

	public PlayerDisplayAnvilTextChangedEvent(String newText, UUID userId) {
		super(userId);
		this.newText = newText;
	}

	public String getNewText() {
		return newText;
	}
}
