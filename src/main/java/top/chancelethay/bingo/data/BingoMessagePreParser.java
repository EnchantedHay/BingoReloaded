package top.chancelethay.bingo.data;

import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import org.jetbrains.annotations.Nullable;

public interface BingoMessagePreParser {

	String parse(@Nullable PlayerHandle player, String message);

	class PassthroughMessagePreParser implements BingoMessagePreParser {

		@Override
		public String parse(@Nullable PlayerHandle player, String message) {
			return message;
		}
	}
}
