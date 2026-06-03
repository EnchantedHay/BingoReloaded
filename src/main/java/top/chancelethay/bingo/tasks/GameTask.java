package top.chancelethay.bingo.tasks;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.data.core.tag.TagDataStorage;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.tasks.data.ItemTask;
import top.chancelethay.bingo.tasks.data.TaskData;
import top.chancelethay.bingo.util.timer.GameTimer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GameTask
{
    private BingoParticipant completedBy;
    private BingoTeam completedByTeam;
    public long completedAt;
    private boolean voided;

    public TaskData data;

    public GameTask(@NotNull TaskData data)
    {
        this.data = data;
        this.completedBy = null;
        this.completedByTeam = null;
        this.voided = false;
        this.completedAt = -1L;
    }

    public static GameTask simpleItemTask(ItemType material, int count) {
        return new GameTask(new ItemTask(material, count));
    }

    public void setVoided(boolean value)
    {
        if (isCompleted())
            return;

        voided = value;
    }

    public boolean isVoided()
    {
        return voided;
    }

    public boolean isCompleted()
    {
        return completedBy != null || completedByTeam != null;
    }

    public ItemTemplate toItem(CardDisplayInfo displayInfo)
    {
        ItemTemplate item;
        // Step 1: create the item and put the new name, description and material on it.
        if (isVoided()) // VOIDED TASK
        {
            item = new ItemTemplate(ItemType.of("structure_void"), null);
            Component[] addedDesc = BingoMessage.VOIDED.asMultiline(NamedTextColor.DARK_GRAY);

            item.setName(getName());
            item.setLore(addedDesc);
            item.setGlowing(true);
        }
        else if (isCompleted()) // COMPLETED TASK
        {
            ItemType completeMaterial = ItemType.of("barrier");

            String timeString = GameTimer.getTimeAsString(completedAt);

            Component[] desc = BingoMessage.COMPLETED_LORE.asMultiline(NamedTextColor.DARK_PURPLE,
                    completedBy.getDisplayName()
                            .color(completedBy.getTeam().getColor())
                            .decorate(TextDecoration.BOLD)
                            .decorate(TextDecoration.ITALIC),
                    Component.text(timeString)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.ITALIC));

            item = new ItemTemplate(completeMaterial, getName(), desc);
        }
        else // DEFAULT TASK
        {
            item = new ItemTemplate(icon(displayInfo), data.getName(), data.getItemDescription()).setDummy(true);
            item.setAmount(data.getRequiredAmount());
        }

        // STEP 2: Add additional stuff like pdc data and glowing effect.

        TagDataStorage storage = new TagDataStorage();
        new GameTaskSerializer().toDataStorage(storage, this);
        item.setExtraData(storage);

        if ((data.shouldItemGlow() || isCompleted()) && !isVoided())
        {
            item.setGlowing(true);
        }

        item.setMaxStackSize(64);
        return item;
    }

    public static @Nullable GameTask fromItem(StackHandle in)
    {
        TagDataStorage store = in.getStorage();
		return store.toSerializable(GameTask.class);
    }

    public static Key getTaskDataKey(String property)
    {
        return BingoReloaded.resourceKey("task." + property);
    }

    public boolean complete(BingoParticipant participant, long gameTime)
    {
        if (isCompleted() || isVoided())
            return false;

        completedByTeam = participant.getTeam();
        completedBy = participant;
        completedAt = gameTime;
        return true;
    }

    public GameTask copy()
    {
        return new GameTask(data);
    }

    public Optional<BingoParticipant> getCompletedByPlayer() {
        return Optional.ofNullable(completedBy);
    }

    public Optional<BingoTeam> getCompletedByTeam() {
        return Optional.ofNullable(completedByTeam);
    }

    public boolean isCompletedByTeam(@NotNull BingoTeam team) {
        return team.equals(completedByTeam);
    }

    public Component getName() {
        if (isVoided())
        {
            TextComponent.Builder nameBuilder = Component.text()
                    .color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH);
            nameBuilder.append(Component.text("A").decorate(TextDecoration.OBFUSCATED));
            nameBuilder.append(data.getName().color(NamedTextColor.DARK_GRAY));
            nameBuilder.append(Component.text("A").decorate(TextDecoration.OBFUSCATED));
            return nameBuilder.build();
        }
        else if (isCompleted()) {
            TextComponent.Builder nameBuilder = Component.text()
                    .color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH);
            nameBuilder.append(data.getName());
            return nameBuilder.build();
        }
        else {
            return data.getName();
        }
    }

    public ItemType icon(CardDisplayInfo displayInfo) {
        return data.getDisplayMaterial(displayInfo);
    }

    public TaskData.TaskType taskType() {
        return data.getType();
    }

}
