package net.Indyuce.mmoitems.stat;

import org.jetbrains.annotations.Nullable;

/**
 * mmoitems
 * 22/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public enum StatsCategory {
    MMOCORE,
    ELEMENTS,
    RESTORE,
    NONE;

    public static final String NBT_PATH = "mmoitems-stats";

    public @Nullable String id() {
        if (this == NONE) return null;
        return name().toLowerCase();
    }
}
