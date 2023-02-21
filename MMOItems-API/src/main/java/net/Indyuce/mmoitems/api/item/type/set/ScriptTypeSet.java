package net.Indyuce.mmoitems.api.item.type.set;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SkillMetadata;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * mmoitems
 * 21/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ScriptTypeSet extends AbstractTypeSet {

    private final Script script;

    public ScriptTypeSet(String name, boolean hasAttackEffect, Script script) {
        super(name, hasAttackEffect);
        this.script = script;
    }

    @Override
    public void apply(AttackMetadata attackData, PlayerData source, LivingEntity target, Weapon weapon) {
        final SkillMetadata skillMetadata = new SkillMetadata(null, source.getMMOPlayerData());
        // TODO: check if the scripts are working
        script.cast(skillMetadata);
    }


    public Script getScript() {
        return script;
    }

    public static ScriptTypeSet load(@NotNull ConfigurationSection section) {
        Validate.notNull(section, "The section cannot be null");
        Validate.isTrue(section.contains("name"), "The section must contain a name");
        Validate.isTrue(section.contains("attack-effect"), "The section must contain a has-attack-effect");
        Validate.isTrue(section.contains("script"), "The section must contain a script");

        final String name = section.getString("name");
        final boolean hasAttackEffect = section.getBoolean("has-attack-effect");
        final Script script = MythicLib.plugin.getSkills().getScriptOrThrow(section.getString("script"));

        return new ScriptTypeSet(name, hasAttackEffect, script);
    }
}
