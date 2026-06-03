package top.chancelethay.bingo.gameloop.vote;

import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.settings.PlayerKit;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class KitCategory extends VoteCategory<PlayerKit>
{
    public KitCategory() {
        super("kit", BingoMessage.OPTIONS_KIT.asPhrase());
    }

    @Override
    @NotNull
    List<String> getValidValues() {
        return Arrays.stream(PlayerKit.values())
                .filter(PlayerKit::isValid)
                .map(kit -> kit.configName)
                .toList();
    }

    @Override
    PlayerKit createResultForValue(String value) {
        return PlayerKit.fromConfig(value);
    }

    @Override
    public Component getValueComponent(String value) {
        return createResultForValue(value).getDisplayName();
    }
}
