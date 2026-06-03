package top.chancelethay.bingo.gui.card;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.api.HotswapCardMenu;
import top.chancelethay.bingo.cards.hotswap.HotswapTaskSlot;
import top.chancelethay.bingo.lib.platform.MenuBoard;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HotswapGenericCardMenu extends GenericCardMenu implements HotswapCardMenu
{
    private List<HotswapTaskSlot> taskHolders;

    public HotswapGenericCardMenu(BingoReloaded bingo, MenuBoard menuBoard, CardDisplayInfo displayInfo, @Nullable Component alternateTitle) {
        super(bingo, menuBoard, displayInfo, alternateTitle);
    }

    public void updateTaskHolders(List<HotswapTaskSlot> holders) {
        this.taskHolders = holders;
    }

    @Override
    public CardMenu copy(@Nullable Component alternateTitle) {
        return new HotswapGenericCardMenu(bingo(), getMenuBoard(), displayInfo(), alternateTitle);
    }

    @Override
    public @NotNull ItemTemplate getItemFromTask(int taskIndex) {
        HotswapTaskSlot holder = taskHolders.get(taskIndex);
        return holder.convertToItem(tasks.get(taskIndex), displayInfo());
    }
}
