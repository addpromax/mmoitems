package net.Indyuce.mmoitems.command.mmoitems.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairCommandTreeNode extends CommandTreeNode {
    public RepairCommandTreeNode(CommandTreeNode parent) {
        super(parent, "repair");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only for players.");
            return CommandResult.FAILURE;
        }

        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();
        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(stack);

        if (!item.hasTag("MMOITEMS_DURABILITY")) {
            Message.REPAIR_COMMAND_UNREPAIRABLE.format(ChatColor.RED).send(player);
            return CommandResult.FAILURE;
        }

        DurabilityItem durItem = new DurabilityItem(player, stack);
        player.getInventory().setItemInMainHand(durItem.addDurability(durItem.getMaxDurability()).toItem());

        Message.REPAIR_COMMAND_SUCCESS.format(ChatColor.GREEN).send(player);
        return CommandResult.SUCCESS;
    }
}
