package top.chancelethay.bingo.gui.item;

import top.chancelethay.bingo.lib.inventory.action.MenuAction;
import top.chancelethay.bingo.tasks.GameTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public class TaskItemAction extends MenuAction
{
    private final GameTask task;

    public TaskItemAction(@NotNull GameTask task) {
        this.task = task;
    }

    @Override
    public void use(ActionArguments arguments) {
        arguments.player().sendMessage(Component.empty());
        arguments.player().sendMessage(task.data.getName().decorate(TextDecoration.BOLD));
        arguments.player().sendMessage(Component.text(" - ").append(task.data.getChatDescription()));
    }
}
