package top.chancelethay.bingo.cards.hotswap;

import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.tasks.GameTask;
import top.chancelethay.bingo.util.timer.GameTimer;
import net.kyori.adventure.text.format.TextColor;

public class SimpleHotswapTask implements HotswapTaskSlot
{
	int fullTime;
    int currentTime;
    boolean recovering = false;

    public SimpleHotswapTask(int recoveryTime) {
        this.currentTime = recoveryTime;
		this.fullTime = recoveryTime;
    }

    @Override
    public boolean isRecovering() {
        return recovering;
    }

    @Override
    public void startRecovering() {
        recovering = true;
    }

    @Override
    public void updateTaskTime() {
        if (recovering) {
            currentTime -= 1;
        }
    }

	@Override
	public int getFullTime() {
		return recovering ? fullTime : -1;
	}

	@Override
    public int getCurrentTime() {
        return currentTime;
    }

    @Override
    public ItemTemplate convertToItem(GameTask task, CardDisplayInfo displayInfo) {
        ItemTemplate item = task.toItem(displayInfo);
        if (isRecovering()) {
            item.addDescription("time", 1, BingoMessage.HOTSWAP_RECOVER.asPhrase(GameTimer.getTimeAsComponent(currentTime)).color(TextColor.fromHexString("#5cb1ff")));
        }
        return item;
    }
}
