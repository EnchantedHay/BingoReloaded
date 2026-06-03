package top.chancelethay.bingo.lib.platform;

import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.api.CardMenu;
import top.chancelethay.bingo.api.TeamDisplay;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.data.record.GameRecordData;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.gameloop.phase.PregameLobby;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.platform.player.SharedDisplay;
import top.chancelethay.bingo.lib.data.core.DataAccessor;
import top.chancelethay.bingo.player.BingoParticipant;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Set;

/**
 * Used by BingoReloaded to set up features that are implemented by each platform separately.
 */
public interface BingoReloadedRuntime {
	DataAccessor getConfigData();
	Collection<DataAccessor> getDataToRegister();
	void setupConfig();

	Set<EntityType> getValidEntityTypesForStatistics();

	record LanguageData(DataAccessor selectedLanguage, DataAccessor backupLanguage){};
	LanguageData getLanguageData(String language);
	void onLanguageUpdated();
	void onConfigReloaded(BingoConfigurationData config);

	void registerActions(BingoConfigurationData config);

	WorldHandle createBingoWorld(String worldName, Key generationOptions);

	ServerSoftware getServerSoftware();

	CardMenu createMenu(boolean textured, CardDisplayInfo displayInfo);
	StackHandle createCardItemForPlayer(BingoParticipant player);

	void openBingoMenu(PlayerHandle player, BingoSession session);
	void openTeamEditor(PlayerHandle player);
	void openBingoCreator(PlayerHandle player);
	void openTeamCardSelect(PlayerHandle player, BingoSession session);
	void openTeamSelector(PlayerHandle player, BingoSession session);
	void openVoteMenu(PlayerHandle player, PregameLobby lobby);
	void openGameHistory(PlayerHandle player, GameRecordData historyData);

	TeamDisplay createTeamDisplay(BingoSession session);
	SharedDisplay gameDisplay();
	SharedDisplay settingsDisplay();
}
