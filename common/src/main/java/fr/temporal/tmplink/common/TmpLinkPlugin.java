package fr.temporal.tmplink.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.temporal.tmplink.common.command.CommandSender;
import fr.temporal.tmplink.common.command.TmpLinkCommand;
import fr.temporal.tmplink.common.config.PluginConfig;
import fr.temporal.tmplink.common.data.*;
import fr.temporal.tmplink.common.http.client.HttpClient;
import fr.temporal.tmplink.common.http.server.HttpServer;
import fr.temporal.tmplink.common.http.server.NettyHttpServer;
import fr.temporal.tmplink.common.logger.LoggerAdapter;
import fr.temporal.tmplink.common.scheduler.SchedulerAdapter;
import fr.temporal.tmplink.common.tasks.FetcherTask;
import fr.temporal.tmplink.common.users.UserManager;
import fr.temporal.tmplink.common.utils.SystemUtils;
import fr.temporal.tmplink.common.utils.UpdateChecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TmpLinkPlugin {

  private static final Gson GSON = new Gson();
  private static final Gson GSON_PRETTY_PRINT = new GsonBuilder().setPrettyPrinting().create();

  private final HttpClient httpClient = new HttpClient(this);
  private final UserManager userManager = new UserManager(this);

  private final TmpLinkCommand command = new TmpLinkCommand(this);

  private final FetcherTask fetcherTask = new FetcherTask(this);
  private final TmpLinkPlatform platform;

  private PluginConfig config = new PluginConfig(null, null);
  private HttpServer httpServer;
  private Path configFile;

  private boolean logCpuError = true;

  public TmpLinkPlugin(TmpLinkPlatform platform) {
    this.platform = platform;
  }

  public void init() {
    this.configFile = this.platform.getDataDirectory().resolve("config.json");

    try (BufferedReader reader = Files.newBufferedReader(this.configFile)) {
      this.config = GSON.fromJson(reader, PluginConfig.class);
    } catch (IOException e) {
      getLogger().error("Error while loading configuration", e);
      return;
    }

    this.httpServer = createHttpServer();

    LocalDateTime start = LocalDateTime.now()
        .truncatedTo(ChronoUnit.MINUTES)
        .plusMinutes(1)
        .plusSeconds(1 + (long) (Math.random() * 30));

    long startDelay = Duration.between(LocalDateTime.now(), start).toMillis();
    long repeatDelay = TimeUnit.MINUTES.toMillis(1);

    getScheduler().executeAsyncRepeating(this.fetcherTask, startDelay, repeatDelay, TimeUnit.MILLISECONDS);

    if (!this.config.isValid()) {
      getLogger().warn("Invalid configuration, please use '/azlink' to setup the plugin.");
      return;
    }

    if (this.config.hasInstantCommands()) {
      this.httpServer.start();
    }

    if (this.config.hasUpdatesCheck()) {
      UpdateChecker updateChecker = new UpdateChecker(this);

      getScheduler().executeAsync(updateChecker::checkUpdates);
    }

    this.httpClient.verifyStatus()
        .thenRun(() -> getLogger().info("Successfully connected to " + this.config.getSiteUrl()))
        .exceptionally(ex -> {
          getLogger().warn("Unable to verify the website connection: " + ex.getMessage());

          return null;
        });
  }

  public void restartHttpServer() {
    if (this.httpServer != null) {
      this.httpServer.stop();
    }

    this.httpServer = createHttpServer();

    this.httpServer.start();
  }

  public void shutdown() {
    getLogger().info("Shutting down scheduler");

    try {
      getScheduler().shutdown();
    } catch (Exception e) {
      getLogger().warn("Error while shutting down scheduler", e);
    }

    if (this.httpServer != null) {
      getLogger().info("Stopping HTTP server");
      this.httpServer.stop();
    }
  }

  public void saveConfig() throws IOException {
    if (!Files.isDirectory(this.platform.getDataDirectory())) {
      Files.createDirectories(this.platform.getDataDirectory());
    }

    try (BufferedWriter writer = Files.newBufferedWriter(this.configFile)) {
      GSON_PRETTY_PRINT.toJson(this.config, writer);
    }
  }

  public TmpLinkCommand getCommand() {
    return this.command;
  }

  public ServerData getServerData(boolean fullData) {
    List<PlayerData> players = this.platform.getOnlinePlayers()
        .map(CommandSender::toData)
        .collect(Collectors.toList());
    int max = this.platform.getMaxPlayers();

    double cpuUsage = getCpuUsage();

    String version = this.platform.getPluginVersion();
    SystemData system = fullData ? new SystemData(SystemUtils.getMemoryUsage(), cpuUsage) : null;
    WorldData world = fullData ? this.platform.getWorldData().orElse(null) : null;
    PlatformData platformData = this.platform.getPlatformData();

    return new ServerData(platformData, version, players, max, system, world, fullData);
  }


  public CompletableFuture<Void> fetch() {
    return this.fetcherTask.fetch();
  }

  public LoggerAdapter getLogger() {
    return this.platform.getLoggerAdapter();
  }

  public SchedulerAdapter getScheduler() {
    return this.platform.getSchedulerAdapter();
  }

  public PluginConfig getConfig() {
    return this.config;
  }

  public TmpLinkPlatform getPlatform() {
    return this.platform;
  }

  public HttpClient getHttpClient() {
    return this.httpClient;
  }

  public HttpServer getHttpServer() {
    return this.httpServer;
  }

  public UserManager getUserManager() {
    return this.userManager;
  }

  protected HttpServer createHttpServer() {
    return new NettyHttpServer(this);
  }

  private double getCpuUsage() {
    try {
      return SystemUtils.getCpuUsage();
    } catch (Throwable t) {
      if (this.logCpuError) {
        this.logCpuError = false;

        getLogger().warn("Error while retrieving CPU usage", t);
      }
    }
    return -1;
  }

  public static Gson getGson() {
    return GSON;
  }

  public static Gson getGsonPrettyPrint() {
    return GSON_PRETTY_PRINT;
  }
}
