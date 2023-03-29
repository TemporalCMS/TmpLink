package fr.temporal.tmplink.common.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.temporal.tmplink.common.TmpLinkPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public class UpdateChecker {
  private static final String RELEASE_URL = "https://api.github.com/repos/TemporalCMS/TmpLink/releases/latest";

  private final TmpLinkPlugin plugin;

  public UpdateChecker(TmpLinkPlugin plugin) {
    this.plugin = plugin;
  }

  public static int compareVersion(String version1, String version2) throws NumberFormatException {
    Objects.requireNonNull(version1, "version1");
    Objects.requireNonNull(version2, "version2");

    String[] version1Parts = parseVersion(version1).split("\\.");
    String[] version2Parts = parseVersion(version2).split("\\.");
    int maxLength = Math.max(version1Parts.length, version2Parts.length);
    for (int i = 0; i < maxLength; i++) {
      int v1 = i < version1Parts.length ? Integer.parseInt(version1Parts[i]) : 0;
      int v2 = i < version2Parts.length ? Integer.parseInt(version2Parts[i]) : 0;

      if (v1 != v2) {
        return Integer.compare(v1,v2);
      }
    }

    return 0;
  }

  private static String parseVersion(String version) {
    return version.replace("v", "").replace("-SNAPSHOT", "");
  }

  public void checkUpdates() {
    try {
      URL url = new URL(RELEASE_URL);
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
        JsonObject jsonObject = TmpLinkPlugin.getGson().fromJson(reader, JsonObject.class);
        JsonElement lastVersionJson = jsonObject.get("tag_name");

        if (lastVersionJson == null) {
          return;
        }

        String currentVersion = this.plugin.getPlatform().getPluginVersion();
        String lastVersion = lastVersionJson.getAsString();

        if (compareVersion(lastVersion, currentVersion) > 0) {
          this.plugin.getLogger().warn("A new update of TmpLink is available: " + lastVersion);
          this.plugin.getLogger().warn("You can download it on https://temporalcms.fr/azlink");
        }
      }
    } catch (Exception ignored) {}
  }
}
