package top.chancelethay.bingo.lib.platform;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class PaperApiHelper {

	private PaperApiHelper(){};

	public static @Nullable WorldPosition worldPosFromLocation(@Nullable Location location) {
		if (location == null) {
			return null;
		}
		return new WorldPosition(new WorldHandle(location.getWorld()), location.x(), location.y(), location.z(), location.getPitch(), location.getYaw());
	}

	public static Location locationFromWorldPos(WorldPosition location) {
		return new Location(location.world().handle(), location.x(), location.y(), location.z(), (float)location.yaw(), (float)location.pitch());
	}


}
