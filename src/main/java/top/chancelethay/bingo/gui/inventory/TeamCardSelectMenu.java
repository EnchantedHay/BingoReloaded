package top.chancelethay.bingo.gui.inventory;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.api.MenuBoard;
import top.chancelethay.bingo.lib.api.item.ItemTypePaper;
import top.chancelethay.bingo.lib.api.player.PlayerHandle;
import top.chancelethay.bingo.lib.inventory.FilterType;
import top.chancelethay.bingo.lib.inventory.PaginatedSelectionMenu;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.player.team.BingoTeam;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamCardSelectMenu extends PaginatedSelectionMenu
{
    private final BingoSession session;

    public TeamCardSelectMenu(MenuBoard board, BingoSession session) {
        super(board, BingoMessage.SHOW_TEAM_CARD_TITLE.asPhrase(), buildTeamOptions(session), FilterType.DISPLAY_NAME);
        this.session = session;
    }

    @Override
    public boolean openOnce() {
        return true;
    }

    @Override
    public void onOptionClickedDelegate(InventoryClickEvent event, ItemTemplate clickedOption, PlayerHandle player) {
        if (!session.canPlayersViewCard()) {
            return;
        }

        Optional<BingoTeam> team = session.teamManager.getActiveTeams().getById(clickedOption.getCompareKey());
        if (team.isPresent() && team.get().getCard().isPresent()) {
            team.get().getCard().get().showInventory(player);
        }
    }

    public static List<ItemTemplate> buildTeamOptions(BingoSession session) {
        List<ItemTemplate> result = new ArrayList<>();
        for (BingoTeam team : session.teamManager.getActiveTeams()) {
            team.getCard().ifPresent(card -> {
                ItemTemplate item = new ItemTemplate(ItemTypePaper.of(Material.LEATHER_CHESTPLATE),
                        BingoReloaded.applyTitleFormat(BingoMessage.SHOW_TEAM_CARD_NAME.asPhrase(team.getColoredName())),
                        INPUT_LEFT_CLICK.append(BingoMessage.SHOW_TEAM_CARD_DESC.asPhrase()))
                        .setLeatherColor(team.getColor())
                        .setCompareKey(team.getIdentifier());
                result.add(item);
            });
        }

        return result;
    }
}
