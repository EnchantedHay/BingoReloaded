package top.chancelethay.bingo.lib.inventory;

public record MenuFilterSettings(FilterType filterType, String name) {

	public static MenuFilterSettings EMPTY = new MenuFilterSettings(FilterType.NONE, "");
}
