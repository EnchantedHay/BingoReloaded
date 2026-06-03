package top.chancelethay.bingo.lib.platform;

import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerInfo;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Thin facade over the running Paper plugin: resources, players, worlds, item stacks,
 * scheduling and the like. A single instance is created at plugin enable and exposed
 * globally via {@link #set} / {@link #get}.
 */
public final class ServerSoftware {

	private static ServerSoftware INSTANCE;

	public static void set(ServerSoftware platform) {
		if (INSTANCE != null) throw new IllegalStateException("Platform already initialized");
		INSTANCE = platform;
	}

	public static ServerSoftware get() {
		if (INSTANCE == null) throw new IllegalStateException("Platform not initialized");
		return INSTANCE;
	}

	private final JavaPlugin plugin;

	public ServerSoftware(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Should be called to get a resource directly embedded in the jar.
	 */
	public @Nullable InputStream getResource(String filePath) {
		return plugin.getResource(filePath);
	}

	/**
	 * Should be used to save a copy of a resource embedded in the jar into the data folder.
	 */
	public void saveResource(String name, boolean replace) {
		plugin.saveResource(name, replace);
	}

	/**
	 * @return the folder where data and special config stuff is saved that is not contained in the default config file.
	 */
	public File getDataFolder() {
		return plugin.getDataFolder();
	}

	public Collection<? extends PlayerHandle> getOnlinePlayers() {
		return Bukkit.getOnlinePlayers().stream().map(PlayerHandle::new).toList();
	}

	public @Nullable PlayerHandle getPlayerFromUniqueId(UUID id) {
		Player p = Bukkit.getPlayer(id);
		if (p == null) {
			return null;
		}
		return new PlayerHandle(p);
	}

	public @Nullable PlayerHandle getPlayerFromName(String name) {
		Player p = Bukkit.getPlayer(name);
		if (p == null) {
			return null;
		}
		return new PlayerHandle(p);
	}

	public @NotNull PlayerInfo getPlayerInfo(UUID playerId) {
		OfflinePlayer offline = Bukkit.getOfflinePlayer(playerId);
		return new PlayerInfo(playerId, offline.getName());
	}

	public @NotNull PlayerInfo getPlayerInfo(String playerName) {
		OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
		return new PlayerInfo(offline.getUniqueId(), playerName);
	}

	public ExtensionInfo getExtensionInfo() {
		return new ExtensionInfo(plugin.getPluginMeta().getName(), plugin.getPluginMeta().getVersion(), plugin.getPluginMeta().getAuthors());
	}

	public ComponentLogger getComponentLogger() {
		return plugin.getComponentLogger();
	}

	public Collection<WorldHandle> getLoadedWorlds() {
		return Bukkit.getWorlds().stream().map(this::fromWorld).toList();
	}

	public @Nullable WorldHandle getWorld(String worldName) {
		return fromWorld(Bukkit.getWorld(worldName));
	}

	public @Nullable WorldHandle getWorld(UUID worldId) {
		return fromWorld(Bukkit.getWorld(worldId));
	}

	public boolean unloadWorld(@NotNull WorldHandle world, boolean save) {
		return Bukkit.unloadWorld(world.handle(), save);
	}

	public StackHandle colorItemStack(StackHandle stack, TextColor color) {
		if (!ItemTemplate.LEATHER_ARMOR.contains(stack.type())) {
			return stack;
		}
		stack.handle().setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.fromRGB(color.value())));
		return stack;
	}

	public boolean areAdvancementsDisabled() {
		return !Bukkit.advancementIterator().hasNext() || Bukkit.advancementIterator().next() == null;
	}

	public ExtensionTask runTaskTimer(long repeatTicks, long startDelayTicks, Consumer<ExtensionTask> consumer) {
		ExtensionTask wrapper = new ExtensionTask();

		Bukkit.getScheduler().runTaskTimer(plugin, (BukkitTask task) -> {
			wrapper.setTask(task);
			consumer.accept(wrapper);
		}, startDelayTicks, repeatTicks);

		return wrapper;
	}

	public ExtensionTask runTask(Consumer<ExtensionTask> consumer) {
		ExtensionTask wrapper = new ExtensionTask();

		Bukkit.getScheduler().runTask(plugin, (BukkitTask task) -> {
			wrapper.setTask(task);
			consumer.accept(wrapper);
		});

		return wrapper;
	}

	public ExtensionTask runTask(long startDelayTicks, Consumer<ExtensionTask> consumer) {
		if (startDelayTicks <= 0) {
			return runTask(consumer);
		}
		else {
			ExtensionTask wrapper = new ExtensionTask();

			Bukkit.getScheduler().runTaskLater(plugin, (BukkitTask task) -> {
				wrapper.setTask(task);
				consumer.accept(wrapper);
			}, startDelayTicks);

			return wrapper;
		}
	}

	/**
	 * Sends command as console.
	 */
	public void sendConsoleCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	private @Nullable WorldHandle fromWorld(@Nullable World world) {
		return world == null ? null : new WorldHandle(world);
	}

	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	public void saveConfig() {
		plugin.saveConfig();
	}

	public void reloadConfig() {
		plugin.reloadConfig();
	}
}
