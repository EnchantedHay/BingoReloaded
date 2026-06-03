package top.chancelethay.bingo.lib.api.player;

import top.chancelethay.bingo.lib.menu.InfoMenu;

public interface SharedDisplay {

	void update(InfoMenu info);

	void addPlayer(PlayerHandle player);

	void removePlayer(PlayerHandle player);

	void clearPlayers();
}
