package fr.temporal.tmplink.bukkit;

import fr.temporal.tmplink.bukkit.command.BukkitCommandExecutor;
import fr.temporal.tmplink.bukkit.command.BukkitCommandSender;
import fr.temporal.tmplink.bukkit.injector.InjectedHttpServer;
import fr.temporal.tmplink.bukkit.integrations.AuthMeIntegration;
import fr.temporal.tmplink.bukkit.integrations.MoneyPlaceholderExpansion;
import fr.temporal.tmplink.common.TmpLinkPlatform;
import fr.temporal.tmplink.common.TmpLinkPlugin;
import fr.temporal.tmplink.common.command.CommandSender;
import fr.temporal.tmplink.common.data.WorldData;
import fr.temporal.tmplink.common.http.server.HttpServer;
import fr.temporal.tmplink.common.logger.JavaLoggerAdapter;
import fr.temporal.tmplink.common.logger.LoggerAdapter;
import fr.temporal.tmplink.common.platform.PlatformInfo;
import fr.temporal.tmplink.common.platform.PlatformType;
import fr.temporal.tmplink.common.scheduler.JavaSchedulerAdapter;
import fr.temporal.tmplink.common.scheduler.SchedulerAdapter;
import fr.temporal.tmplink.common.tasks.TpsTask;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public final class TmpLinkBukkitPlugin extends JavaPlugin implements TmpLinkPlatform {
  private final TpsTask tpsTask = new TpsTask();
  private final SchedulerAdapter scheduler = new JavaSchedulerAdapter(
      r -> getServer().getScheduler().runTask(this, r),
      r -> getServer().getScheduler().runTaskAsynchronously(this, r)
  );

  private TmpLinkPlugin plugin;
  private LoggerAdapter logger;

  @Override
  public void onLoad() {
    this.logger = new JavaLoggerAdapter(getLogger());
  }

  @Override
  public void onEnable() {
    try {
      Class.forName("com.google.gson.JsonObject");
      Class.forName("io.netty.channel.Channel");
    } catch (ClassNotFoundException e) {
      this.logger.error("Your server version is not compatible with this version of TmpLink!");
      this.logger.error("Please download TmpLink Legacy on https://temporal.fr/tmplink");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    this.plugin = new TmpLinkPlugin(this) {
      @Override
      protected HttpServer createHttpServer() {
        if (plugin.getConfig().getHttpPort() == getServer().getPort()) {
          return new InjectedHttpServer(TmpLinkBukkitPlugin.this);
        }

        return super.createHttpServer();
      }
    };
    this.plugin.init();

    getCommand("tmplink").setExecutor(new BukkitCommandExecutor(this.plugin));

    getServer().getScheduler().runTaskTimer(this, this.tpsTask, 0, 1);
    saveDefaultConfig();

    if (getConfig().getBoolean("authme-integration")
        && getServer().getPluginManager().getPlugin("AuthMe") != null) {
      getServer().getPluginManager().registerEvents(new AuthMeIntegration(this), this);
    }

    if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
      MoneyPlaceholderExpansion.enable(this);
    }
  }

  @Override
  public void onDisable() {
    if (this.plugin != null) {
      this.plugin.shutdown();
    }
  }

  @Override
  public TmpLinkPlugin getPlugin() {
    return this.plugin;
  }

  @Override
  public LoggerAdapter getLoggerAdapter() {
    return this.logger;
  }

  @Override
  public SchedulerAdapter getSchedulerAdapter() {
    return this.scheduler;
  }

  @Override
  public PlatformType getPlatformType() {
    return PlatformType.BUKKIT;
  }

  @Override
  public PlatformInfo getPlatformInfo() {
    return new PlatformInfo(getServer().getName(), getServer().getVersion());
  }

  @Override
  public String getPluginVersion() {
    return getDescription().getVersion();
  }

  @Override
  public Path getDataDirectory() {
    return getDataFolder().toPath();
  }

  @Override
  public Optional<WorldData> getWorldData() {
    int loadedChunks = getServer().getWorlds().stream()
        .mapToInt(w -> w.getLoadedChunks().length)
        .sum();

    int entities = getServer().getWorlds().stream()
        .mapToInt(w -> w.getEntities().size())
        .sum();

    return Optional.of(new WorldData(this.tpsTask.getTps(), loadedChunks, entities));
  }

  @Override
  public Stream<CommandSender> getOnlinePlayers() {
    if (getConfig().getBoolean("ignore-vanished-players", false)) {
      return getServer().getOnlinePlayers()
          .stream()
          .filter(this::isPlayerVisible)
          .map(BukkitCommandSender::new);
    }

    return getServer().getOnlinePlayers().stream().map(BukkitCommandSender::new);
  }

  @Override
  public int getMaxPlayers() {
    return getServer().getMaxPlayers();
  }

  @Override
  public void dispatchConsoleCommand(String command) {
    getServer().dispatchCommand(getServer().getConsoleSender(), command);
  }

  private boolean isPlayerVisible(Player player) {
    for (MetadataValue meta : player.getMetadata("vanished")) {
      if (meta.asBoolean()) {
        return false;
      }
    }
    return true;
  }
}
