package fr.temporal.tmplink.bukkit.command;

import fr.temporal.tmplink.common.TmpLinkPlugin;
import fr.temporal.tmplink.common.command.TmpLinkCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class BukkitCommandExecutor extends TmpLinkCommand implements TabExecutor {

  public BukkitCommandExecutor(TmpLinkPlugin plugin) {
    super(plugin);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    execute(new BukkitCommandSender(sender), args);

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return tabComplete(new BukkitCommandSender(sender), args);
  }
}