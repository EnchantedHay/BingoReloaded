package top.chancelethay.bingo.util.timer;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.data.BingoMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CounterTimer extends GameTimer
{
    @Override
    public Component getTimeDisplayMessage(boolean asSeconds)
    {
        String timeString = asSeconds ? GameTimer.getSecondsString(getTime()) : GameTimer.getTimeAsString(getTime());
        return BingoMessage.DURATION.asPhrase(Component.text(timeString).color(NamedTextColor.WHITE))
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD);
    }

    @Override
    public int getStartDelay()
    {
        return 0;
    }

    @Override
    public int getUpdateInterval()
    {
        return BingoReloaded.ONE_SECOND;
    }

    @Override
    public int getStep()
    {
        return 1;
    }
}
