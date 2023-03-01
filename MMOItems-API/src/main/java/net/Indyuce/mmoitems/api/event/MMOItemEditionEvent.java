package net.Indyuce.mmoitems.api.event;

import net.Indyuce.mmoitems.api.edition.input.ChatEdition;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sent when a MMOItem is edited, by a player, using `/mi browse` GUI
 */
public class MMOItemEditionEvent extends PlayerEvent {

    /**
     * @return What of MMOItem is being edited
     */
    @NotNull public ChatEdition getEditionInformation() { return editionInformation; }
    @NotNull final ChatEdition editionInformation;

    /**
     * @return Message (I guess)
     */
    @NotNull public String getEditionMessage() { return editionMessage; }
    @NotNull final String editionMessage;

    /**
     * @return Old value
     */
    @Nullable public RandomStatData getLegacyValue() { return legacyValue; }
    @Nullable final RandomStatData legacyValue;

    /**
     * @param who Player doing edition of MMOItems
     * @param editionInformation What MMOItem is being edited
     */
    public MMOItemEditionEvent(@NotNull Player who, @NotNull ChatEdition editionInformation, @NotNull String editionMessage, @Nullable RandomStatData legacyValue) {
        super(who);
        this.editionInformation = editionInformation;
        this.editionMessage = editionMessage;
        this.legacyValue = legacyValue;
    }

    //region Event Standard
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    //endregion
}
