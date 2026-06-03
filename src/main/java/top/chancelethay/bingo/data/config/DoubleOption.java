package top.chancelethay.bingo.data.config;

import top.chancelethay.bingo.lib.data.core.DataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DoubleOption extends ConfigurationOption<Double>
{
    public DoubleOption(String configName) {
        super(configName);
    }

    @Override
    public Optional<Double> fromString(String value) {
        try {
            double val = Double.parseDouble(value);
            return Optional.of(val);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public void toDataStorage(DataStorage storage, @NotNull Double value) {
        storage.setDouble(getConfigName(), value);
    }
}
