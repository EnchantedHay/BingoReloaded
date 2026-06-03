package top.chancelethay.bingo.player;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.lib.api.ServerSoftware;
import top.chancelethay.bingo.lib.api.WorldPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerRespawnManager
{
    private final Map<UUID, DeadPlayer> deadPlayers;

	public PlayerRespawnManager(ServerSoftware platform, int respawnPeriodSeconds) {
        this.deadPlayers = new HashMap<>();
        //TODO: Maybe only have the task running if there are dead players?
		platform.runTaskTimer(0, BingoReloaded.ONE_SECOND, (t) -> {
			for (var p : new HashSet<>(deadPlayers.keySet())) {
				DeadPlayer player = deadPlayers.get(p);
				if (System.currentTimeMillis() > player.deathTime + respawnPeriodSeconds * 1000L) {
					deadPlayers.remove(p);
				}
			}
		});
	}

    private record DeadPlayer(WorldPosition deathLocation, Long deathTime)
    {
    }

    public void addPlayer(UUID playerId, WorldPosition deathLocation) {
        deadPlayers.put(playerId, new DeadPlayer(deathLocation, System.currentTimeMillis()));
    }

    public Optional<WorldPosition> removeDeadPlayer(UUID playerId) {
        DeadPlayer player = deadPlayers.remove(playerId);
        if (player == null) {
            return Optional.empty();
        }

        return Optional.of(player.deathLocation);
    }
}