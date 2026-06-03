package top.chancelethay.bingo.lib.util;

import top.chancelethay.bingo.lib.platform.ServerSoftware;
import net.kyori.adventure.text.Component;

public class ConsoleMessenger
{
    // Resolved lazily on each call rather than cached at class-load, so this class
    // carries no dependency on ServerSoftware having been initialized before it loads.
    private static ServerSoftware platform() {
        return ServerSoftware.get();
    }

    public static void log(String message) {
        platform().getComponentLogger().info(
                ComponentUtils.MINI_BUILDER.deserialize(message));
    }

    public static void warn(String message) {
        platform().getComponentLogger().warn(
                ComponentUtils.MINI_BUILDER.deserialize(message));
    }

    public static void error(String message) {
        platform().getComponentLogger().error(
                ComponentUtils.MINI_BUILDER.deserialize(message));
    }

    public static void log(String message, String source) {
        platform().getComponentLogger().info(
                Component.text("(" + source + "): ")
                        .append(ComponentUtils.MINI_BUILDER.deserialize(message)));
    }

    public static void warn(String message, String source) {
        platform().getComponentLogger().warn(
                Component.text("(" + source + "): ")
                        .append(ComponentUtils.MINI_BUILDER.deserialize(message)));
    }

    public static void error(String message, String source) {
        platform().getComponentLogger().error(
                Component.text("(" + source + "): ")
                        .append(ComponentUtils.MINI_BUILDER.deserialize(message)));
    }

    public static void log(Component message) {
        platform().getComponentLogger().info(message);
    }

    public static void log(Component message, String source) {
        platform().getComponentLogger().info(
                Component.text("(" + source + "): ")
                        .append(message));
    }

    public static void log(Component message, Component source) {
        platform().getComponentLogger().info(
                Component.text("(").append(source).append(Component.text("): "))
                        .append(message));
    }

    public static void bug(String message, Class<?> source) {
        platform().getComponentLogger().error(
                ComponentUtils.MINI_BUILDER.deserialize(message)
                        .append(Component.text("; Source: " + source.getName() + " (Please report!)")));
    }

    public static void bug(String message, Object source) {
        platform().getComponentLogger().error(
                ComponentUtils.MINI_BUILDER.deserialize(message)
                        .append(Component.text("; Source: " + source.getClass().getName() + " (Please report!)")));
    }

    public static void bug(Component message, Object source) {
        platform().getComponentLogger().error(
                message.append(Component.text("; Source: " + source.getClass().getName() + " (Please report!)")));
    }
}
