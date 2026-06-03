package top.chancelethay.bingo.data.config;

import top.chancelethay.bingo.lib.data.core.DataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BooleanOption extends ConfigurationOption<Boolean>
{
    public BooleanOption(String configName) {
        super(configName);
    }

    @Override
    public Optional<Boolean> fromString(String value) {
        return Optional.of(value.equals("true"));
    }

    @Override
    public void toDataStorage(DataStorage storage, @NotNull Boolean value) {
        storage.setBoolean(getConfigName(), value);
    }
}
