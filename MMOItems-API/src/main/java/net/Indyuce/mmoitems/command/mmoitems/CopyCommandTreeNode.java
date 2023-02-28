package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CopyCommandTreeNode extends CommandTreeNode {
	public CopyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "copy");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(new Parameter("<new-id>", (a, b) -> {
		}));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		if (!MMOItemType.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.GRAY
					+ "to see all the available item types.");
			return CommandResult.FAILURE;
		}

		MMOItemType type = MMOItemType.get(args[1]);
		ConfigFile config = type.getConfigFile();
		String id1 = args[2].toUpperCase().replace("-", "_");
		if (!config.getConfig().contains(id1)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + id1 + ".");
			return CommandResult.FAILURE;
		}

		String id2 = args[3].toUpperCase();
		if (config.getConfig().contains(id2)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is already an item called " + id2 + "!");
			return CommandResult.FAILURE;
		}

		config.getConfig().set(id2, config.getConfig().getConfigurationSection(id1));
		config.save();
		MMOItems.plugin.getTemplates().requestTemplateUpdate(type, id2);

		if (sender instanceof Player)
			new ItemEdition((Player) sender, MMOItems.plugin.getTemplates().getTemplate(type, id2)).open();
		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "You successfully copied " + id1 + " to " + id2 + "!");
		return CommandResult.SUCCESS;
	}
}
