package top.chancelethay.bingo.data;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.data.core.DataAccessor;
import top.chancelethay.bingo.settings.CustomKit;
import top.chancelethay.bingo.settings.PlayerKit;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class CustomKitData
{
    private final DataAccessor data = BingoReloaded.getDataAccessor("data/kits");

    public boolean assignCustomKit(Component kitName, PlayerKit slot, PlayerHandle player)
    {
        if (data.contains(slot.configName))
            return false;

        data.setSerializable(slot.configName, CustomKit.class, CustomKit.fromPlayerInventory(player, kitName, slot));
        data.saveChanges();
        return true;
    }

    public boolean removeCustomKit(PlayerKit slot)
    {
        if (!data.contains(slot.configName))
            return false;

        data.erase(slot.configName);
        data.saveChanges();

        return true;
    }

    public @Nullable CustomKit getCustomKit(PlayerKit slot)
    {
        return data.getSerializable(slot.configName, CustomKit.class);
    }
}
