package top.chancelethay.bingo.gameloop.vote;

import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.settings.gamemode.BingoGamemode;
import top.chancelethay.bingo.settings.gamemode.BingoGamemodes;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GamemodeCategory extends VoteCategory<BingoGamemode>
{
    public GamemodeCategory() {
        super("gamemode", BingoMessage.OPTIONS_GAMEMODE.asPhrase());
    }

    @Override
    @NotNull
    List<String> getValidValues() {
        return BingoGamemodes.GAMEMODES.keySet().stream().toList();
    }

    @Override
    @Nullable
    BingoGamemode createResultForValue(String value) {
        return BingoGamemodes.fromDataString(value, true);
    }

    @Override
    public Component getValueComponent(String value) {
        BingoGamemode mode = createResultForValue(value);
        return mode == null ? Component.text("<null>") : mode.asComponent();
    }
}
