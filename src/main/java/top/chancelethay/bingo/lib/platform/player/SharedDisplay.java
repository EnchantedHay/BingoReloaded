package top.chancelethay.bingo.lib.platform.player;

import top.chancelethay.bingo.lib.display.InfoMenu;

public interface SharedDisplay {

	void update(InfoMenu info);

	void addPlayer(PlayerHandle player);

	void removePlayer(PlayerHandle player);

	void clearPlayers();
}
