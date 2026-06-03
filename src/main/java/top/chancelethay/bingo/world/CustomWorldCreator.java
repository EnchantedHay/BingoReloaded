package top.chancelethay.bingo.world;

import top.chancelethay.bingo.lib.api.ServerSoftware;
import top.chancelethay.bingo.lib.api.WorldHandle;
import top.chancelethay.bingo.lib.api.WorldHandlePaper;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;


public class CustomWorldCreator
{
    public static WorldHandle createWorld(ServerSoftware platform, String worldName, @Nullable Key generationSettingsResource) {
        String worldFolder = getWorldsFolder(platform);
        World world = CustomWorldCreator_V1_21_11.createBingoWorld(worldFolder + worldName, generationSettingsResource);
        return new WorldHandlePaper(world);
    }

    private static String getWorldsFolder(ServerSoftware platform) {
        return platform.getDataFolder().getPath().replace("\\", "/") + "/worlds/";
    }
}
