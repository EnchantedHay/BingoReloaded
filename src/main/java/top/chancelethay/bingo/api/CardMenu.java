package top.chancelethay.bingo.api;

import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.tasks.GameTask;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CardMenu
{
    void setInfo(Component title, Component... description);
    void updateTasks(List<GameTask> tasks);
    void open(PlayerHandle entity);
    List<GameTask> getTasks();

    /**
     * FIXME: when menus can have changeable titles (i.e. when menu builders get added)
     */
    CardMenu copy(@Nullable Component alternateTitle);
    CardDisplayInfo displayInfo();
}
