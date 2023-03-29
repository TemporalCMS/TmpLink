package fr.temporal.tmplink.common;

import fr.temporal.tmplink.common.command.CommandSender;
import fr.temporal.tmplink.common.data.PlatformData;
import fr.temporal.tmplink.common.data.WorldData;
import fr.temporal.tmplink.common.logger.LoggerAdapter;
import fr.temporal.tmplink.common.platform.PlatformInfo;
import fr.temporal.tmplink.common.platform.PlatformType;
import fr.temporal.tmplink.common.scheduler.SchedulerAdapter;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface TmpLinkPlatform {

  TmpLinkPlugin getPlugin();

  LoggerAdapter getLoggerAdapter();

  SchedulerAdapter getSchedulerAdapter();

  PlatformType getPlatformType();

  PlatformInfo getPlatformInfo();

  String getPluginVersion();

  Path getDataDirectory();

  Stream<CommandSender> getOnlinePlayers();

  int getMaxPlayers();

  default Optional<WorldData> getWorldData() {
    return Optional.empty();
  }

  void dispatchConsoleCommand(String command);

  default PlatformData getPlatformData() {
    return new PlatformData(getPlatformType(), getPlatformInfo());
  }

  default void prepareDataAsync() {
  }
}
