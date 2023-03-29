package fr.temporal.tmplink.bukkit.command;

import fr.temporal.tmplink.common.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BukkitCommandSender implements CommandSender {

  private final org.bukkit.command.CommandSender sender;

  public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
    this.sender = sender;
  }

  @Override
  public String getName() {
    return this.sender.getName();
  }

  @Override
  public UUID getUuid() {
    if (this.sender instanceof Entity) {
      return ((Entity) this.sender).getUniqueId();
    }

    return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void sendMessage(String message) {
    this.sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
  }

  @Override
  public boolean hasPermission(String permission) {
    return this.sender.hasPermission(permission);
  }
}