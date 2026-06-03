package top.chancelethay.bingo.data.serializers;

import top.chancelethay.bingo.data.BingoLobby;
import top.chancelethay.bingo.lib.platform.WorldPosition;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BingoLobbySerializer implements DataStorageSerializer<BingoLobby> {

	@Override
	public void toDataStorage(@NotNull DataStorage storage, @NotNull BingoLobby value) {
		storage.setWorldPosition("spawn", value.spawnPosition());
	}

	@Override
	public @Nullable BingoLobby fromDataStorage(@NotNull DataStorage storage) {
		WorldPosition spawn = storage.getWorldPosition("spawn");
		return new BingoLobby(spawn);
	}
}
