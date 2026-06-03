package top.chancelethay.bingo.lib.data.core.tag.adapters;

import top.chancelethay.bingo.lib.platform.ServerSoftware;
import top.chancelethay.bingo.lib.platform.WorldHandle;
import top.chancelethay.bingo.lib.platform.WorldPosition;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldPositionStorageSerializer implements DataStorageSerializer<WorldPosition>
{
    @Override
    public void toDataStorage(@NotNull DataStorage storage, @NotNull WorldPosition value) {
        storage.setUUID("world", value.world().uniqueId());
        storage.setDouble("x", value.x());
        storage.setDouble("y", value.y());
        storage.setDouble("z", value.z());
//        storage.setFloat("yaw", value.getYaw());
//        storage.setFloat("pitch", value.getPitch());
    }

    @Override
    public WorldPosition fromDataStorage(@NotNull DataStorage storage) {
        UUID id = storage.getUUID("world");
        if (id == null) {
            return null;
        }
        WorldHandle world = ServerSoftware.get().getWorld(id);
        if (world == null) {
            return null;
        }

        double x = storage.getDouble("x", 0.0D);
        double y = storage.getDouble("y", 0.0D);
        double z = storage.getDouble("z", 0.0D);
        float yaw = storage.getFloat("yaw", 0.0f);
        float pitch = storage.getFloat("pitch", 0.0f);
        return new WorldPosition(world, x, y, z);
    }
}
