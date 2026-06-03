package top.chancelethay.bingo.lib.platform.item;

import top.chancelethay.bingo.lib.platform.ServerSoftware;
import top.chancelethay.bingo.lib.data.core.tag.TagDataStorage;
import top.chancelethay.bingo.lib.data.core.tag.TagTree;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import top.chancelethay.bingo.lib.util.PDCHelper;
import top.chancelethay.bingo.util.ItemHelper;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class StackHandle {

	private static final NamespacedKey CUSTOM_DATA_KEY = new NamespacedKey(
			ServerSoftware.get().getExtensionInfo().name().toLowerCase(),
			"custom");

	private final @NotNull ItemStack stack;

	public StackHandle(@Nullable ItemStack stack) {
		this.stack = stack != null ? stack : new ItemStack(Material.AIR);
	}

	public static StackHandle createFromTemplate(ItemTemplate template, boolean hideAttributes) {
		return new StackBuilder().buildItem(template, hideAttributes);
	}

	public static StackHandle create(ItemType type, int amount) {
		return new StackHandle(new ItemStack(type.handle(), amount));
	}

	public static StackHandle create(ItemType type) {
		return create(type, 1);
	}

	public static StackHandle empty() {
		return new StackHandle(new ItemStack(Material.AIR));
	}

	public static StackHandle deserializeBytes(byte[] bytes) {
		return new StackHandle(ItemStack.deserializeBytes(bytes));
	}

	public static byte[] serializeBytes(StackHandle stack) {
		return stack.handle().serializeAsBytes();
	}

	public ItemType type() {
		return new ItemType(stack.getType());
	}

	public int amount() {
		return stack.getAmount();
	}

	public Component customName() {
		return stack.getItemMeta().displayName();
	}

	public List<Component> lore() {
		return stack.getItemMeta().lore();
	}

	public String compareKey() {
		if (stack.getItemMeta() == null) {
			return "";
		}
		return PDCHelper.getStringFromPdc(stack.getItemMeta().getPersistentDataContainer(), "compare_key");
	}

	public boolean isTool() {
		return ItemHelper.isTool(stack.getType());
	}

	public boolean isArmor() {
		return ItemHelper.isArmor(stack.getType());
	}

	public void setAmount(int newAmount) {
		stack.setAmount(newAmount);
	}

	@Override
	public StackHandle clone() {
		return new StackHandle(stack.clone());
	}

	/**
	 * Clears and adds the newStorage to the stack using the custom_data component.
	 * @param newStorage Source of data to copy over.
	 */
	public void setStorage(TagDataStorage newStorage) {
		stack.editPersistentDataContainer(container -> {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				newStorage.getTree().getPayload(out);
				byte[] bytes = out.toByteArray();

				container.set(CUSTOM_DATA_KEY, PersistentDataType.BYTE_ARRAY, bytes);
			} catch (IOException e) {
				ConsoleMessenger.bug("Custom Data (in setStorage()) exception: " + e.getMessage(), this);
			}
		});
	}

	/**
	 * @return existing store containing custom item data
	 * 		   or a new store when the stack does not currently have any custom data assigned.
	 * 		   Could be an expensive operation in some implementations!
	 */
	public @NotNull TagDataStorage getStorage() {
		byte[] bytes = stack.getItemMeta().getPersistentDataContainer()
				.get(CUSTOM_DATA_KEY, PersistentDataType.BYTE_ARRAY);

		if (bytes == null) {
			return new TagDataStorage();
		}

		try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
			TagTree tree = TagTree.fromPayload(in);
			return new TagDataStorage(tree);
		} catch (IOException e) {
			ConsoleMessenger.bug("Custom Data (in getStorage()) exception: " + e.getMessage(), this);
			return new TagDataStorage();
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public void setCooldown(Key cooldownGroup, double cooldownTimeSeconds) {
		stack.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown((float)cooldownTimeSeconds).cooldownGroup(cooldownGroup).build());
	}

	public ItemStack handle() {
		return stack;
	}
}
