package top.chancelethay.bingo.lib.data.core;

import top.chancelethay.bingo.lib.platform.ServerSoftware;

/**
 * Specific yaml data accessor for the config.yml file provided by Bukkit.
 */
public class ConfigDataAccessor extends YamlDataStorage implements DataAccessor
{
    private final ServerSoftware platform;

    public ConfigDataAccessor(ServerSoftware platform) {
        super(platform.getConfig());
        this.platform = platform;
    }

    /**
     * Not needed since this is the main config file.
     * @return empty string
     */
    @Override
    public String getLocation() {
        return "";
    }

    @Override
    public String getFileExtension() {
        return ".yml";
    }

    @Override
    public void load() {
        platform.reloadConfig();
        config = platform.getConfig();
    }

    @Override
    public void saveChanges() {
        platform.saveConfig();
    }

    @Override
    public boolean isInternalReadOnly() {
        return false;
    }
}
