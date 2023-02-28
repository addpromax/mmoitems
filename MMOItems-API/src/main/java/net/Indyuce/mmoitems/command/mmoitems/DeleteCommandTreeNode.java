package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DeleteCommandTreeNode extends CommandTreeNode {
	public DeleteCommandTreeNode(CommandTreeNode parent) {
		super(parent, "delete");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!MMOItemType.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED
					+ " to see all the available item types.");
			return CommandResult.FAILURE;
		}

		MMOItemType type = MMOItemType.get(args[1]);
		String id = args[2].toUpperCase().replace("-", "_");
		if (!MMOItems.plugin.getTemplates().hasTemplate(type, id)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + id + ".");
			return CommandResult.FAILURE;
		}

		MMOItems.plugin.getTemplates().deleteTemplate(type, id);
		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "You successfully deleted " + id + ".");
		return CommandResult.SUCCESS;
	}
}
