package top.chancelethay.bingo.lib.menu;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

public class InfoMenu {

	protected final Map<String, Component[]> registeredFields = new HashMap<>();

	public void addField(String key, Component... text) {
		registeredFields.put(key, text);
	}

}
