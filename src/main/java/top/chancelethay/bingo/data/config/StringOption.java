package top.chancelethay.bingo.data.config;

import top.chancelethay.bingo.lib.data.core.DataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class StringOption extends ConfigurationOption<String>
{
    public StringOption(String configName) {
        super(configName);
    }

    @Override
    public Optional<String> fromString(String value) {
		if (value.isEmpty() || value.equals("null")) {
			return Optional.empty();
		}
        return Optional.of(value);
    }

    @Override
    public void toDataStorage(DataStorage storage, @NotNull String value) {
        storage.setString(getConfigName(), value);
    }
}