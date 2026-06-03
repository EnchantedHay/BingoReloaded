package top.chancelethay.bingo.api;

import top.chancelethay.bingo.lib.platform.player.PlayerHandle;

public interface TeamDisplay {

	void update();

	void clearTeamsForPlayer(PlayerHandle player);

	void reset();
}
