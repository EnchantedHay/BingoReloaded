package top.chancelethay.bingo.gui;

import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.data.TeamData;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.inventory.MenuBoard;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.inventory.FilterType;
import top.chancelethay.bingo.lib.inventory.PaginatedSelectionMenu;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.lib.util.ComponentUtils;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.BingoPlayer;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.player.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectionMenu extends PaginatedSelectionMenu
{
    private final BingoSession session;
    private final TeamManager teamManager;

    private static final Component PLAYER_PREFIX = ComponentUtils.MINI_BUILDER.deserialize("<gray><bold> ┗ </bold></gray><white>");

    public TeamSelectionMenu(MenuBoard manager, BingoSession session) {
        super(manager, BingoMessage.OPTIONS_TEAM.asPhrase(), new ArrayList<>(), FilterType.NONE);
        this.session = session;
        this.teamManager = session.teamManager;
    }

    @Override
    public void onOptionClickedDelegate(InventoryClickEvent event, ItemTemplate clickedOption, PlayerHandle player) {
        BingoParticipant participant = teamManager.getPlayerAsParticipant(player);
        if (participant == null)
        {
            participant = new BingoPlayer(player, session);
        }

        if (clickedOption.getCompareKey().equals("item_auto")) {
            teamManager.addMemberToTeam(participant, "auto");
            reopen(player);
            return;
        } else if (clickedOption.getCompareKey().equals("item_leave")) {
            teamManager.removeMemberFromTeam(participant);
            reopen(player);
            return;
        }

        teamManager.addMemberToTeam(participant, clickedOption.getCompareKey());
        reopen(player);
    }

    @Override
    public void beforeOpening(PlayerHandle player) {
        super.beforeOpening(player);

        List<ItemTemplate> optionItems = new ArrayList<>();
        ItemTemplate autoItem = new ItemTemplate(ItemType.of(Material.NETHER_STAR), BingoMessage.TEAM_AUTO.asPhrase().color(TextColor.fromHexString("#fdffa8")).decorate(TextDecoration.BOLD, TextDecoration.ITALIC))
                .setCompareKey("item_auto");
        optionItems.add(autoItem);
        optionItems.add(new ItemTemplate(ItemType.of(Material.TNT), BingoMessage.OPTIONS_LEAVE.asPhrase().color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD, TextDecoration.ITALIC))
                .setGlowing(true).setCompareKey("item_leave"));

        var allTeams = teamManager.getJoinableTeams();
        for (String teamId : allTeams.keySet()) {
            boolean playersTeam = false;
            TeamData.TeamTemplate teamTemplate = allTeams.get(teamId);

            boolean teamIsFull = false;
            List<Component> players = new ArrayList<>();

            for (BingoTeam team : teamManager.getActiveTeams()) {
                if (!team.getIdentifier().equals(teamId))
                    continue;

                for (BingoParticipant participant : team.getMembers()) {
                    players.add(PLAYER_PREFIX.append(participant.getDisplayName()));
                    if (participant.getId().equals(player.uniqueId())) {
                        playersTeam = true;
                    }
                }

                if (teamManager.getMaxTeamSize() == team.getMembers().size()) {
                    teamIsFull = true;
                }
            }

            Component teamStatus;
            if (teamIsFull) {
                teamStatus = BingoMessage.FULL_TEAM_DESC.asPhrase().color(NamedTextColor.RED);
            } else {
                teamStatus = BingoMessage.JOIN_TEAM_DESC.asPhrase().color(NamedTextColor.GREEN);
            }

            optionItems.add(ItemTemplate.createColoredLeather(teamTemplate.color(), ItemType.of(Material.LEATHER_HELMET))
                    .setName(teamTemplate.nameComponent().color(teamTemplate.color()).decorate(TextDecoration.BOLD))
                    .setLore(players.toArray(Component[]::new))
                    .setCompareKey(teamId)
                    .setGlowing(playersTeam)
                    .addDescription("status", 1, teamStatus));
        }

        clearItems();
        addItemsToSelect(optionItems);
    }
}
