package top.chancelethay.bingo.lib.platform;


import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.inventory.Menu;
import org.bukkit.plugin.java.JavaPlugin;

public interface MenuBoard {

	JavaPlugin plugin();

	void open(Menu menu, PlayerHandle player);

	void close(Menu menu, PlayerHandle player);

	void closeAll(PlayerHandle player);
}
