package top.chancelethay.bingo.lib.menu;

import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.api.player.SharedDisplay;

public class EmptyDisplay implements SharedDisplay {

	@Override
	public void update(InfoMenu info) {

	}

	@Override
	public void addPlayer(PlayerHandle player) {

	}

	@Override
	public void removePlayer(PlayerHandle player) {

	}

	@Override
	public void clearPlayers() {

	}
}
