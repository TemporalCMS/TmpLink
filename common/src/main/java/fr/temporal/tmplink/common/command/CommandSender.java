package fr.temporal.tmplink.common.command;

import fr.temporal.tmplink.common.data.PlayerData;

import java.util.UUID;

public interface CommandSender {

  String getName();

  UUID getUuid();

  void sendMessage(String message);

  boolean hasPermission(String permission);

  default PlayerData toData() {
    return new PlayerData(getName(), getUuid());
  }
}
