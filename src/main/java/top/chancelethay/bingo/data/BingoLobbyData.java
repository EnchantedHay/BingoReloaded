package top.chancelethay.bingo.data;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.lib.platform.WorldPosition;
import top.chancelethay.bingo.lib.data.core.DataAccessor;
import org.jetbrains.annotations.Nullable;

public class BingoLobbyData {
	private final DataAccessor data = BingoReloaded.getDataAccessor("data/lobby");


	public boolean isEnabled() {
		return data.getBoolean("enabled", false);
	}

	public void create(WorldPosition location) {
		data.setSerializable("lobby", BingoLobby.class, new BingoLobby(location));
		data.setBoolean("enabled", true);
		data.saveChanges();
	}

	public void remove() {
		if (!isEnabled()) {
			return;
		}

		data.setBoolean("enabled", false);
		data.erase("lobby");
		data.saveChanges();
	}

	public @Nullable BingoLobby getCreatedLobby() {
		if (!isEnabled()) {
			return null;
		}

		return data.getSerializable("lobby", BingoLobby.class);
	}
}
