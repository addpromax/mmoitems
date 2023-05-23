package net.Indyuce.mmoitems.util;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * mmoitems
 *
 * @author Roch Blondiaux
 * @date 24/10/2022
 */
public class PluginUtils {

    public PluginUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void isDependencyPresent(@NotNull String name, @NotNull Consumer<Void> callback) {
        if (Bukkit.getPluginManager().getPlugin(name) != null)
            callback.accept(null);
    }

    public static void hookDependencyIfPresent(@NotNull String name, @NotNull Consumer<Void> callback) {
        if (Bukkit.getPluginManager().getPlugin(name) == null)
            return;
        callback.accept(null);
        MMOItems.plugin.getLogger().log(Level.INFO, String.format("Hooked onto %s", name));
    }

    public static String capitalize(@NotNull String text) {
        if (text.length() == 0)
            return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String capitalizeAllWorlds(@NotNull String text) {
        StringBuilder builder = new StringBuilder();
        for (String word : text.split(" "))
            builder.append(capitalize(word)).append(" ");
        return builder.toString().trim();
    }

}
