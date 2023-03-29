package fr.temporal.tmplink.common.users;

import fr.temporal.tmplink.common.TmpLinkPlugin;
import fr.temporal.tmplink.common.data.UserInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

  private final Map<String, UserInfo> usersByName = new ConcurrentHashMap<>();
  private final TmpLinkPlugin plugin;

  public UserManager(TmpLinkPlugin plugin) {
    this.plugin = plugin;
  }

  public Optional<UserInfo> getUserByName(String name) {
    return Optional.ofNullable(this.usersByName.get(name));
  }

  public void addUser(UserInfo user) {
    this.usersByName.put(user.getName(), user);
  }

  public CompletableFuture<UserInfo> editMoney(UserInfo user, String action, double amount) {
    return this.plugin.getHttpClient().editMoney(user, action, amount)
        .thenApply(result -> {
          user.setMoney(result.getNewBalance());
          return user;
        });
  }
}
