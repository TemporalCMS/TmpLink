package fr.temporal.tmplink.common.data;

import java.util.UUID;

public class PlayerData {

  private final String name;
  private final UUID uuid;

  public PlayerData(String name, UUID uuid) {
    this.name = name;
    this.uuid = uuid;
  }

  public String getName() {
    return this.name;
  }

  public UUID getUuid() {
    return this.uuid;
  }
}