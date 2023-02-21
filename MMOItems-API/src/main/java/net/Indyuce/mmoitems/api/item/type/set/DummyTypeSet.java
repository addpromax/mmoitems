package net.Indyuce.mmoitems.api.item.type.set;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.LivingEntity;

/**
 * mmoitems
 * 21/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class DummyTypeSet extends AbstractTypeSet {

    public DummyTypeSet(String name) {
        super(name, false);
    }

    @Override
    public void apply(AttackMetadata attackData, PlayerData source, LivingEntity target, Weapon weapon) {
    }
}
