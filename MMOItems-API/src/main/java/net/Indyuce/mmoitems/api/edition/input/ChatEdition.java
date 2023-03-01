package net.Indyuce.mmoitems.api.edition.input;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.Edition;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.event.MMOItemEditionEvent;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class ChatEdition extends PlayerInputHandler implements Listener {

	/**
	 * Allows to retrieve player input using chat messages
	 * 
	 * @param edition The type of data being edited
	 */
	public ChatEdition(Edition edition) {
		super(edition);

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public void close() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void a(AsyncPlayerChatEvent event) {
		if (getPlayer() != null && event.getPlayer().equals(getPlayer())) {
			@Nullable RandomStatData legacy;
			if (getEdition() instanceof StatEdition && getEdition().getInventory() instanceof EditionInventory) { legacy = ((EditionInventory) getEdition().getInventory()).getEdited().getBaseItemData().get(((StatEdition) getEdition()).getStat()); } else { legacy = null; }

			// Send it to sync
			ChatEdition capitalQuestion = this;
			(new BukkitRunnable() {
				@Override
				public void run() {

					// Send it to sync
					MMOItemEditionEvent ent = new MMOItemEditionEvent(event.getPlayer(), capitalQuestion, event.getMessage(), legacy);
					Bukkit.getServer().getPluginManager().callEvent(ent);
				}
			}).runTask(MMOItems.plugin);

			// Actually perform edition
			event.setCancelled(true);
			registerInput(event.getMessage());
		}
	}

	// cancel stat edition when opening any gui
	@EventHandler
	public void b(InventoryOpenEvent event) {
		if (event.getPlayer().equals(getPlayer()))
			close();
	}
}
