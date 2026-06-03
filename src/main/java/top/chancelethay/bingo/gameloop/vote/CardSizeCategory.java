package top.chancelethay.bingo.gameloop.vote;

import top.chancelethay.bingo.cards.CardSize;
import top.chancelethay.bingo.data.BingoMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CardSizeCategory extends VoteCategory<CardSize>
{
    public CardSizeCategory() {
        super("cardsize", BingoMessage.OPTIONS_CARDSIZE.asPhrase());
    }

    @Override
    @NotNull
    List<String> getValidValues() {
        return List.of("3", "5");
    }

    @Override
    CardSize createResultForValue(String value) {
        try {
            int cardWidth = Integer.parseInt(value);
            return CardSize.fromWidth(cardWidth);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    @Override
    public Component getValueComponent(String value) {
        return CardSize.fromWidth(Integer.parseInt(value)).asComponent().color(NamedTextColor.YELLOW);
    }
}
