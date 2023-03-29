package fr.temporal.tmplink.common.scheduler;

@FunctionalInterface
public interface CancellableTask {
  void cancel();
}
