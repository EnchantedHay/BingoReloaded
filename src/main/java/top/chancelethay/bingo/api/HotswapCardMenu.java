package top.chancelethay.bingo.api;

import top.chancelethay.bingo.cards.hotswap.HotswapTaskSlot;

import java.util.List;

public interface HotswapCardMenu extends CardMenu
{
    void updateTaskHolders(List<HotswapTaskSlot> taskHolders);
}
