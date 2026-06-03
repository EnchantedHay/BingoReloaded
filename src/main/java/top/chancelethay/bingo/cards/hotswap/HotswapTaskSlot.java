package top.chancelethay.bingo.cards.hotswap;

import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.tasks.GameTask;

public interface HotswapTaskSlot
{
    boolean isRecovering();
    void startRecovering();

    void updateTaskTime();
	int getFullTime();
    int getCurrentTime();

    ItemTemplate convertToItem(GameTask task, CardDisplayInfo displayInfo);
}
