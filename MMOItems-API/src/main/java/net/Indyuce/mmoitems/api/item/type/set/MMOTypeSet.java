package net.Indyuce.mmoitems.api.item.type.set;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;

/**
 * mmoitems
 * 20/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface MMOTypeSet {

    String getName();

    void apply(AttackMetadata attackData, PlayerData source, LivingEntity target, Weapon weapon);

    boolean hasAttackEffect();

    default void applyAttackEffect(AttackMetadata attackMeta, PlayerData damager, LivingEntity target, Weapon weapon) {
        if (this.hasAttackEffect())
            this.apply(attackMeta, damager, target, weapon);
    }

}
