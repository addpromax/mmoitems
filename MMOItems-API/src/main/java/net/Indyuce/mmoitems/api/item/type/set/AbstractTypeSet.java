package net.Indyuce.mmoitems.api.item.type.set;

import org.jetbrains.annotations.ApiStatus;

/**
 * mmoitems
 * 21/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@ApiStatus.Internal
public abstract class AbstractTypeSet implements MMOTypeSet {

    private final String name;
    private final boolean hasAttackEffect;

    public AbstractTypeSet(String name, boolean hasAttackEffect) {
        this.name = name;
        this.hasAttackEffect = hasAttackEffect;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasAttackEffect() {
        return hasAttackEffect;
    }
}
