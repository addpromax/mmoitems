package net.Indyuce.mmoitems.api.util;

import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The DamageTypeRestriction [DTR] modifier has several built-in features, so it's easier
 * to keep them all in this one place, since they also include functions to use them:
 * <br><br>
 * + Translate the trigger name depending on the DTR  <br>
 * + Change color of damage types / scalings in modifiers       <br>
 * +
 */
public class DamageTypeRestrictionSettings {

    /**
     * Read the values directly off a configuration section
     *
     * @param config The configuration section, supposedly in the plugin config.yml
     */
    public DamageTypeRestrictionSettings(@Nullable ConfigurationSection config) {
        if (config == null) {
            //DTR//MythicCraftingManager.log("\u00a78DTR\u00a7a SET\u00a7c Null config provided");
            return; }

        /*
         * damage-type-restrictions:
         *
         *   damage-type-translations:
         *      MAGIC: "Magic"
         *      PHYSICAL: "Melee"
         *      PROJECTILE: "Ranged"
         *      WEAPON: "Weapon"
         *      SKILL: "Skill"
         *      UNARMED: "Unarmed"
         *      ON_HIT: "Reaction"
         *      MINION: "Minion"
         *      DOT: "Lingering"
         *
         *   attack-type-translations:
         *      WEAPON: "Attack"
         *      SKILL: "Ability Hit"
         *      NEITHER: "Damage"
         *      BOTH: "Ability-Assisted Attack"
         *
         *   damage-type-colors:
         *      MAGIC: "&9"
         *      PHYSICAL: "&8"
         *      WEAPON: "&7"
         *      SKILL: "&f"
         *      PROJECTILE: "&a"
         *      UNARMED: "&e"
         *      ON_HIT: "&0"
         *      MINION: "&d"
         *      DOT: "&3"
         */
        ConfigurationSection damageTypeTranslations = config.isConfigurationSection("damage-type-translations") ? config.getConfigurationSection("damage-type-translations") : null;
        ConfigurationSection attackTypeTranslations = config.isConfigurationSection("attack-type-translations") ? config.getConfigurationSection("attack-type-translations") : null;
        if (damageTypeTranslations == null || attackTypeTranslations == null) { advancedTriggerDisplay = false; } else {
            //DTR//MythicCraftingManager.log("\u00a78DTR\u00a7a SET\u00a7a Accepted advanced trigger display");

            // Both are defined, use advanced trigger display
            advancedTriggerDisplay = true;

            // Translate damage types
            damageNames.put(DamageType.MAGIC, damageTypeTranslations.getString("MAGIC", "Magic"));
            damageNames.put(DamageType.PHYSICAL, damageTypeTranslations.getString("PHYSICAL", "Melee"));
            damageNames.put(DamageType.PROJECTILE, damageTypeTranslations.getString("PROJECTILE", "Projectile"));
            damageNames.put(DamageType.WEAPON, damageTypeTranslations.getString("WEAPON", "Weapon"));
            damageNames.put(DamageType.SKILL, damageTypeTranslations.getString("SKILL", "Skill"));
            damageNames.put(DamageType.UNARMED, damageTypeTranslations.getString("UNARMED", "Unarmed"));
            damageNames.put(DamageType.ON_HIT, damageTypeTranslations.getString("ON_HIT", "Reaction"));
            damageNames.put(DamageType.MINION, damageTypeTranslations.getString("MINION", "Minion"));
            damageNames.put(DamageType.DOT, damageTypeTranslations.getString("DOT", "Lingering"));

            // Translate attack types
            attackNames.put(AttackType.WEAPON, attackTypeTranslations.getString("WEAPON", "Attack"));
            attackNames.put(AttackType.SKILL, attackTypeTranslations.getString("SKILL", "Ability Hit"));
            attackNames.put(AttackType.BOTH, attackTypeTranslations.getString("BOTH", "Ability-Assisted Attack"));
            attackNames.put(AttackType.NEITHER, attackTypeTranslations.getString("NEITHER", "Damage"));
        }

        ConfigurationSection damageTypeColors = config.isConfigurationSection("damage-type-colors") ? config.getConfigurationSection("damage-type-colors") : null;
        if (damageTypeColors == null) { advancedRecoloring = false; } else {
            //DTR//MythicCraftingManager.log("\u00a78DTR\u00a7a SET\u00a7a Accepted advanced recoloring");

            // Colors are defined
            advancedRecoloring = true;

            // This information actually already exists
            for (DamageType dt : DamageType.values()) {

                // Well is there an override?
                String colour = damageTypeColors.getString(dt.toString(), null);
                if (colour == null) { continue; }

                // Just match the name of the damage type, default to its default color
                damageColor.put(dt, colour);
            }
        }
    }

    /**
     * @return If trigger names will be translated depending on the DTR, this has the
     *         advantage of being user-friendly at the disadvantage of having to configure
     *         names of each modifier combination ~ WEAPON MAGIC !SKILL for example would be
     *         'Staff Attack'
     */
    public boolean isAdvancedTriggerDisplay() { return advancedTriggerDisplay; }
    boolean advancedTriggerDisplay = true;

    /**
     * @return If the color of damage type modifiers in modifier names
     *         and trigger names will be recolored when actually displaying
     *         into the item.
     */
    public boolean isAdvancedRecoloring() { return advancedRecoloring; }
    boolean advancedRecoloring = true;

    /**
     * Example:
     * PROJECTILE -> Ranged
     *
     * @param type The damage type you intend to translate
     *
     * @return The player-friendly name off this damage type
     */
    @NotNull public String getDamageName(@NotNull DamageType type) { return damageNames.getOrDefault(type, SilentNumbers.titleCaseConversion(type.toString().replace("-", " ").replace("_", " "))); }
    @NotNull HashMap<DamageType, String> damageNames = new HashMap<>();

    /**
     * Example:
     * SKILL -> Ability Hit
     *
     * @param type The attack type you intend to translate
     *
     * @return The player-friendly name off this attack type
     */
    @NotNull public String getAttackName(@NotNull AttackType type) { return attackNames.getOrDefault(type, "Damage"); }
    @NotNull HashMap<AttackType, String> attackNames = new HashMap<>();

    /**
     * Example:
     * SKILL &f (default) -> <#FEEFEF> (specified in config)
     *
     * @param type The damage you intend to get its color
     *
     * @return The 'translated' color of this damage type, to
     *         override the default damage colors mostly.
     */
    @NotNull public String getDamageColor(@NotNull DamageType type) { return damageColor.getOrDefault(type, SilentNumbers.titleCaseConversion(type.toString().replace("-", " ").replace("_", " "))); }
    @NotNull HashMap<DamageType, String> damageColor = new HashMap<>();

    @NotNull public static final String SKMOD_DAMAGE_TYPE_DAMAGE = "\u00a7o■";
    @NotNull public static final String SKMOD_DAMAGE_TYPE_BLACK = "\u00a7c!";
    @NotNull public static final String SKMOD_DAMAGE_TYPE_AND = "\u00a77 ";
    @NotNull public static final String SKMOD_DAMAGE_TYPE_COMMA = "\u00a77,";
    @NotNull public static final String SKMOD_DAMAGE_TYPE_OR = "\u00a77/";

    /**
     * Usually the displayed name of the trigger is just... the name of the trigger.
     * <p>
     * However, when using the Damage Type skill modifier, this can be misleading;
     * for example, the {@link TriggerType#ATTACK} will no longer trigger with any attack.
     * </p>
     * This method will rename it correctly; for example: WEAPON MAGIC = Magic Attack
     * <p></p>
     * This only supports the trigger {@link TriggerType#ATTACK}
     *
     * @param trigger		The trigger by which this skill fires
     * @param attackType	The encoded skill modifier value
     *
     * @return The way the trigger should display in lore.
     */
    @NotNull public String getTriggerDisplayName(@NotNull TriggerType trigger, double attackType) {

        // Use the default?
        String triggerDisplayName = MMOItems.plugin.getLanguage().getCastingModeName(trigger);

        // If no skill modifiers are used, or the config option is disabled
        if (attackType == 0 || !isAdvancedTriggerDisplay()) {
            //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a7c No advanced trigger display\u00a7e " + attackType);
            return triggerDisplayName; }

        // Currently, only ATTACK trigger is supported
        if (!TriggerType.ATTACK.equals(trigger)) {
            //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a7c Not a supported trigger");
            return triggerDisplayName; }

        boolean named = false;
        boolean orMode = false;
        if (attackType < 0) { orMode = true; attackType *= -1; }
        String separatorSymbol = (orMode ? SKMOD_DAMAGE_TYPE_OR : SKMOD_DAMAGE_TYPE_AND);

        // Decode
        ArrayList<DamageType> white = DamageType.getWhitelist(attackType);
        ArrayList<DamageType> black = DamageType.getBlacklist(attackType);

        // Currently, only ATTACK trigger is supported
        if (white.isEmpty()) {
            //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a7c Empty whitelist, blacklist is not supported");
            return triggerDisplayName; }

        // Special names sector
        if (TriggerType.ATTACK.equals(trigger)) {
            //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a77 Identified as the ATTACK trigger");

            // Very specific overrides
            if (white.size() == 1 && white.contains(DamageType.MINION)) {

                // Minion Attack
                triggerDisplayName = getDamageName(DamageType.MINION) + SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.WEAPON);
                named = true;

            } else if  (white.size() == 1 && white.contains(DamageType.DOT)) {

                // Lingering Attack
                triggerDisplayName = getDamageName(DamageType.DOT) + SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.WEAPON);
                named = true;

            } else if (white.size() == 2 && white.contains(DamageType.MINION) && white.contains(DamageType.PROJECTILE)) {

                // Minion Ranged Attack
                triggerDisplayName = getDamageName(DamageType.MINION) + SKMOD_DAMAGE_TYPE_AND + getDamageName(DamageType.PROJECTILE) + SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.WEAPON);
                named = true;

            } else if (white.size() == 2 && white.contains(DamageType.MINION) && white.contains(DamageType.MAGIC)) {

                // Minion Magic Attack
                triggerDisplayName = getDamageName(DamageType.MINION) + SKMOD_DAMAGE_TYPE_AND + getDamageName(DamageType.MAGIC) + SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.WEAPON);
                named = true;

            } else {

                // Skill, Attack, or generic damage
                boolean isWeapon = white.contains(DamageType.WEAPON);
                boolean isSkill = white.contains(DamageType.SKILL);
                boolean both = isWeapon && isSkill;
                boolean neither = !isWeapon && !isSkill;

                // Elemental type
                StringBuilder builder = new StringBuilder();
                for (DamageType whitelisted : white) {

                    // Ignore weapon and skill
                    if (whitelisted == DamageType.WEAPON ||
                            whitelisted == DamageType.SKILL) {
                        continue; }

                    // Append separator
                    if (builder.length() > 1) { builder.append(separatorSymbol); }

                    // Append the type
                    builder
                            //.append(damageColors(whitelisted.getColor()))	// Sawala doesn't think colours are gud
                            .append(getDamageName(whitelisted));
                }

                String latter;
                String built = builder.toString();
                String former = "";

                // Requires any other damage type?
                if (built.length() > 0) {
                    if (neither) { latter = SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.NEITHER); }
                    else if (both) { latter = SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.BOTH); }
                    else if (isWeapon) { latter = SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.WEAPON); }
                    else { latter = SKMOD_DAMAGE_TYPE_AND + getAttackName(AttackType.SKILL); }

                // Only characterized by weapon
                } else {

                    if (neither) { latter = getAttackName(AttackType.NEITHER); }
                    else if (both) { latter = getAttackName(AttackType.BOTH); }
                    else if (isWeapon) { latter = getAttackName(AttackType.WEAPON); }
                    else { latter = getAttackName(AttackType.SKILL); }
                }

                //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a77 Former:\u00a73 " + former);
                //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a77 Built:\u00a73 " + built);
                //APP//MythicCraftingManager.log("\u00a78ABT\u00a73 APP\u00a77 Latter:\u00a73 " + latter);

                named = true;
                triggerDisplayName = former + built + latter;
            }
        }

        // Just display the damage type restriction as squares
        else if (TriggerType.KILL_ENTITY.equals(trigger)) {

            // Elemental type
            StringBuilder builder = new StringBuilder();
            for (DamageType whitelisted : white) {

                // Append separator
                if (builder.length() > 1) { builder.append(separatorSymbol); }

                // Append the type
                builder
                        //.append(damageColors(whitelisted.getColor()))	// Sawala doesn't think colours are gud
                        .append(getDamageName(whitelisted));
            }

            named = true;
            triggerDisplayName = builder + SKMOD_DAMAGE_TYPE_AND + triggerDisplayName;
        }

        // No special name just default it
        if (!named) {

            // Append that
            triggerDisplayName += " " + damageTypeRestrictionDisplay(separatorSymbol, white, black);
        }

        return triggerDisplayName;
    }

    /**
     * @param damageTypeRestriction Number that encodes for the damage type restriction
     *
     * @return A nice chain of colored boxes (real) that represents this damage type restriction.
     */
    @NotNull String damageTypeRestrictionDisplay(double damageTypeRestriction) {

        boolean orMode = false;
        if (damageTypeRestriction < 0) { orMode = true; damageTypeRestriction *= -1; }
        String separatorSymbol = (orMode ? SKMOD_DAMAGE_TYPE_OR : SKMOD_DAMAGE_TYPE_AND);

        // Decode
        ArrayList<DamageType> white = DamageType.getWhitelist(damageTypeRestriction);
        ArrayList<DamageType> black = DamageType.getBlacklist(damageTypeRestriction);

        // Just build the string man
        return damageTypeRestrictionDisplay(separatorSymbol, white, black);
    }

    /**
     *
     * @param separatorSymbol Separator symbol to use between whitelisted damage types
     * @param white Damage types whitelisted
     * @param black Damage types blacklisted
     *
     * @return A nice chain of colored boxes (real) that represents this damage type restriction.
     */
    @NotNull String damageTypeRestrictionDisplay(@NotNull String separatorSymbol, @NotNull ArrayList<DamageType> white, @NotNull ArrayList<DamageType> black) {

        StringBuilder append = new StringBuilder();
        for (DamageType w : white) {

            // Append separator
            if (append.length() > 1) { append.append(separatorSymbol); }

            // Append damage
            append.append(damageColors(w.getColor())).append(SKMOD_DAMAGE_TYPE_DAMAGE);
        }

        // Separator for blacklist
        if (append.length() > 1 && black.size() > 0) { append.append(SKMOD_DAMAGE_TYPE_COMMA); }

        for (DamageType b : black) {

            // Append separator
            if (append.length() > 1) { append.append(SKMOD_DAMAGE_TYPE_AND); }

            // Append damage
            append.append(SKMOD_DAMAGE_TYPE_BLACK).append(damageColors(b.getColor())).append(SKMOD_DAMAGE_TYPE_DAMAGE);
        }

        return append.toString();
    }

    /**
     * @param in The colored string in the default format
     *
     * @return Color overridden by user-specified counterpart.
     */
    @NotNull public String damageColors(@Nullable String in) {
        //SDC//MythicCraftingManager.log("\u00a78ABT\u00a7c SDC\u00a77 Recoloring\u00a7b " + in);
        if (in == null) { return ""; }
        if (!isAdvancedRecoloring()) { return in; }

        /*
         * Sawala's agony color replacements
         *
         * Because I literally had everything consistent in &8 &a &9 and he was like &4 &2 <HEX0038C2>;
         * what on earth even is <HEX0038C2> ffs for magic damage some ugly ass deep blue (I sleep)
         */
        for (DamageType ty : damageColor.keySet()) {

            //SDC//MythicCraftingManager.log("\u00a78ABT\u00a7c SDC\u00a7e +\u00a77 Damage Type\u00a7b " + ty.toString());
            //SDC//MythicCraftingManager.log("\u00a78ABT\u00a7c SDC\u00a7e +\u00a77 Default Col\u00a7b " + ty.getColor() + "O");
            //SDC//MythicCraftingManager.log("\u00a78ABT\u00a7c SDC\u00a7e +\u00a77 Override Cl\u00a7b " + getDamageColor(ty) + "O");

            // Both & and §
            in = in.replace(ty.getColor().replace('\u00a7', '&'), getDamageColor(ty));
            in = in.replace(ty.getColor(), getDamageColor(ty));
        }
        //SDC//MythicCraftingManager.log("\u00a78ABT\u00a7c SDC\u00a77 Result\u00a7b " + in);

        // he he ha ha
        return in;
    }
}

/**
 * A way to classify and translate the result of an {@link io.lumine.mythic.lib.damage.AttackMetadata},
 */
enum AttackType {

    /**
     * Dealing damage with weapons (real), Attacks.
     */
    WEAPON,

    /**
     * Dealing damage with abilities, Ability Hits.
     */
    SKILL,

    /**
     * No information on skill or weapon damage,
     * treated as just generic Damage.
     */
    NEITHER,

    /**
     * Both skill and weapon damage types are present,
     * this would make sense if the lore of the ability
     * implies that your weapon is being used in the attack.
     * <br><br>
     * Compare simply casting a fireball, vs simply hitting
     * with a baton, vs coating the baton with magic fire and
     * then attacking with it. <br>
     * The last case would be an 'Ability-Assisted Attack'
     */
    BOTH
}