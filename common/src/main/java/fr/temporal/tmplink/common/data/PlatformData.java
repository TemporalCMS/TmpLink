package fr.temporal.tmplink.common.data;

import fr.temporal.tmplink.common.platform.PlatformInfo;
import fr.temporal.tmplink.common.platform.PlatformType;

public class PlatformData {

  private final PlatformType type;
  private final String name;
  private final String version;

  public PlatformData(PlatformType type, PlatformInfo info) {
    this.type = type;
    this.name = info.getName();
    this.version = info.getVersion();
  }

  public PlatformType getType() {
    return this.type;
  }

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }
}