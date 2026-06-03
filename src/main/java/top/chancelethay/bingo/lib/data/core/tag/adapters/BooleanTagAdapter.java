package top.chancelethay.bingo.lib.data.core.tag.adapters;

import top.chancelethay.bingo.lib.data.core.tag.Tag;
import top.chancelethay.bingo.lib.data.core.tag.TagAdapter;
import top.chancelethay.bingo.lib.data.core.tag.TagDataType;
import org.jetbrains.annotations.NotNull;

public class BooleanTagAdapter implements TagAdapter<Boolean, Byte>
{
    @Override
    public TagDataType<Byte> getBaseType() {
        return TagDataType.BYTE;
    }

    @Override
    public @NotNull Boolean fromTag(Tag<Byte> node) {
        return node.getValue() == (byte) 1;
    }

    @Override
    public @NotNull Tag<Byte> toTag(Boolean value) {
        return new Tag.ByteTag(value ? (byte)1 : (byte)0);
    }
}
