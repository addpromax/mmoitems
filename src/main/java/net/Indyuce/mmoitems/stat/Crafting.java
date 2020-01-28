package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.MMORecipeChoice;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.CraftingEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Crafting extends ItemStat {
	public Crafting() {
		super(new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial()), "Crafting", new String[] { "The crafting recipes of your item.", "Changing a recipe requires &o/mi reload recipes&7." }, "crafting", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new CraftingEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the crafting edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all crafting recipes.");
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info)
	{
		String type = (String) info[0];
		
		if(type.equals("recipe")) {
			int slot = (int) info[2];

			if(MMORecipeChoice.validate(inv.getPlayer(), message)) {
				if(((String) info[1]).equals("shaped")) {
					List<String> newList = config.getConfig().getStringList(inv.getItemId() + ".crafting.shaped.1");
					String[] newArray = newList.get((int) Math.floor(slot / 3)).split("\\ ");
					newArray[slot % 3] = message;
					newList.set((int) Math.floor(slot / 3), (newArray[0] + " " + newArray[1] + " " + newArray[2]));
					
					config.getConfig().set(inv.getItemId() + ".crafting.shaped.1", newList);
					inv.registerItemEdition(config);
				}
				else {
					config.getConfig().set(inv.getItemId() + ".crafting.shapeless.1.item" + (slot + 1), message);
					inv.registerItemEdition(config);
				}
			}
		}
		else if(type.equals("item")) {
			if(MMORecipeChoice.validate(inv.getPlayer(), message)) {
				Bukkit.getScheduler().runTask(MMOItems.plugin, new Runnable() {
					@Override
					public void run() {
						new StatEdition(inv, false, ItemStat.CRAFTING, "time", info[1], message).enable("Write in the chat the cooktime (in ticks) for your recipe.", "Format: '[INTEGER]'");
					}
				});
			}
		}
		else if(type.equals("time")) {
			int time;
			try {
				time = Integer.parseInt(message);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}
			
			Bukkit.getScheduler().runTask(MMOItems.plugin, new Runnable() {
				@Override
				public void run() {
					new StatEdition(inv, ItemStat.CRAFTING, "exp", info[1], time, info[2]).enable("Write in the chat the experience given for your recipe.", "Format: '[FLOAT]'");
				}
			});
		}
		else if(type.equals("exp")) {
			double exp;
			try {
				exp = Double.parseDouble(message);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}
			
			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.item", info[3]);
			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.time", (int) info[2]);
			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.experience", exp);
			inv.registerItemEdition(config);
		}
		else
			MMOItems.plugin.getLogger().warning("Something went wrong!");
		
		return true;
	}
	
	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config)
	{ return true; }
	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data)
	{ return true; }
	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {}
}
