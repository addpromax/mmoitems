package net.Indyuce.mmoitems.command.mmoitems.list;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class TypeCommandTreeNode extends CommandTreeNode {
	public TypeCommandTreeNode(CommandTreeNode parent) {
		super(parent, "type");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Item Types "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + ChatColor.LIGHT_PURPLE + " Item Types " + ChatColor.DARK_GRAY + ""
				+ ChatColor.STRIKETHROUGH + "]-----------------");
		for (MMOItemType type : MMOItems.plugin.getTypes().getAll())
			sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + type.getName() + " (" + type.getId() + ")");
		return CommandResult.SUCCESS;
	}
}
