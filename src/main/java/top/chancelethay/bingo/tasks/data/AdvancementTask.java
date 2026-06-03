package top.chancelethay.bingo.tasks.data;

import top.chancelethay.bingo.api.CardDisplayInfo;
import top.chancelethay.bingo.api.TaskDisplayMode;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.lib.platform.AdvancementHandle;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.util.ComponentUtils;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashSet;
import java.util.Set;

public record AdvancementTask(AdvancementHandle advancement, Set<String> tags) implements TaskData {

	public AdvancementTask(AdvancementHandle advancement) {
		this(advancement, new HashSet<>());
	}

	@Override
	public TaskType getType() {
		return TaskType.ADVANCEMENT;
	}

	@Override
	public Component getName() {
		var builder = Component.text().append(Component.text("["))
				.color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC);

		if (advancement == null) {
			ConsoleMessenger.log("Could not get advancement, returning null!");
			builder.append(Component.text("no advancement?"));
		} else {
			builder.append(ComponentUtils.advancementTitle(advancement));
		}
		builder.append(Component.text("]"));
		return builder.build();
	}

	@Override
	public Component[] getItemDescription() {
		return BingoMessage.LORE_ADVANCEMENT.asMultiline(NamedTextColor.DARK_AQUA);
	}

	// This method exists because advancement descriptions can contain newlines,
	// which makes it impossible to use as item names or descriptions without getting a missing character.
	@Override
	public Component getChatDescription() {
		return ComponentUtils.advancementDescription(advancement)
				.color(NamedTextColor.DARK_AQUA);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AdvancementTask that = (AdvancementTask) o;
		if (advancement == null) {
			return that.advancement == null;
		}
		return advancement.key().equals(that.advancement.key());
	}

	@Override
	public int hashCode() {
		return advancement.key().hashCode();
	}

	@Override
	public boolean isTaskEqual(TaskData other) {
		return this.equals(other);
	}

	@Override
	public boolean shouldItemGlow() {
		return true;
	}

	@Override
	public ItemType getDisplayMaterial(CardDisplayInfo context) {
		if (context.advancementDisplay() == TaskDisplayMode.GENERIC_TASK_ITEMS || advancement().displayIcon() == null) {
			return ItemType.of("filled_map");
		} else {
			return advancement().displayIcon();
		}
	}

	@Override
	public int getRequiredAmount() {
		return 1;
//        // We need a little NMS voo-doo magic
//        return ((CraftAdvancement)advancement).getHandle().value().requirements().requirements().size();
	}

	@Override
	public TaskData setRequiredAmount(int newAmount) {
		return this;
	}
}
