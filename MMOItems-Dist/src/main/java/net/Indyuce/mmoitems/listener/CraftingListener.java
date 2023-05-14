package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.api.crafting.event.MythicCraftItemEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.CraftMMOItemEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onCraft(MythicCraftItemEvent event) {

        ItemStack result = null;
        ItemStack firstResult = event.getCache().getResultOfOperation().getResultInventory().getFirst();
        if (firstResult != null){
            result = firstResult;
        }
        if (result == null){
            return;
        }
        if (!NBTItem.get(result).hasType()){
            return;
        }



        CraftMMOItemEvent e = new CraftMMOItemEvent(PlayerData.get(((Player) event.getTrigger().getWhoClicked())), result);
        Bukkit.getPluginManager().callEvent(e);

        event.setCancelled(e.isCancelled());

    }

//    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
//    public void onCraft(CraftMMOItemEvent event){
//        ItemStack result = event.getResult();
//        if (result == null){
//            throw new RuntimeException("No Valid Result for CraftMMOItemEvent of " + event.getPlayerData().getPlayer().getName());
//        }
//        if (!NBTItem.get(result).hasType()){
//            event.setCancelled(true);
//            return;
//        }
//
//    }
}
