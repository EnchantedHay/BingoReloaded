package top.chancelethay.bingo.data.serializers;

import top.chancelethay.bingo.data.TeamData;
import top.chancelethay.bingo.lib.data.core.DataStorage;
import top.chancelethay.bingo.lib.data.core.DataStorageSerializer;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamTemplateStorageSerializer implements DataStorageSerializer<TeamData.TeamTemplate>
{
    @Override
    public void toDataStorage(@NotNull DataStorage storage, TeamData.@NotNull TeamTemplate value) {
        storage.setString("name", value.stringName());
        storage.setString("color", value.color().asHexString());
    }

    @Override
    public @Nullable TeamData.TeamTemplate fromDataStorage(@NotNull DataStorage storage) {
        return new TeamData.TeamTemplate(
                storage.getString("name", ""),
                TextColor.fromHexString(storage.getString("color", "#808080"))
        );
    }
}
