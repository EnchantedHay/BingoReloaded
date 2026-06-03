package top.chancelethay.bingo.lib.data.core.tag.adapters;

import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.data.core.tag.Tag;
import top.chancelethay.bingo.lib.data.core.tag.TagAdapter;
import top.chancelethay.bingo.lib.data.core.tag.TagDataType;
import org.jetbrains.annotations.NotNull;

public class ItemStackTagAdapter implements TagAdapter<StackHandle, byte[]>
{
    @Override
    public TagDataType<byte[]> getBaseType() {
        return TagDataType.BYTE_ARRAY;
    }

    @Override
    public @NotNull StackHandle fromTag(Tag<byte[]> tag) {
		if (tag.getValue().length == 0) {
			return StackHandle.create(ItemType.AIR);
		} else {
			return StackHandle.deserializeBytes(tag.getValue());
		}
    }

    @Override
    public @NotNull Tag<byte[]> toTag(@NotNull StackHandle value) {
		if (value.type().isAir()) {
			return new Tag.ByteArrayTag(new byte[]{});
		} else {
			return new Tag.ByteArrayTag(StackHandle.serializeBytes(value));
		}
    }
}
