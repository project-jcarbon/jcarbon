package jcarbon;

import static java.util.stream.Collectors.joining;
import static jcarbon.util.LoggerUtil.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class JCarbonReport {
  private static final Logger logger = getLogger();
  private final HashMap<Class<?>, List<?>> dataSignals = new HashMap<>();

  /** Checks if there is a signal for the class. */
  public boolean hasSignal(Class<?> cls) {
    return dataSignals.keySet().stream().anyMatch(cls::equals);
  }

  /** Shallow copy of the signals added. */
  public Set<Class<?>> getSignalTypes() {
    return new HashSet<>(dataSignals.keySet());
  }

  /** Shallow copy of the signal list. */
  public <T> List<T> getSignal(Class<T> cls) {
    if (hasSignal(cls)) {
      return new ArrayList<>((List<T>) dataSignals.get(cls));
    }
    return List.of();
  }

  /** Deep copy of the storage. */
  public Map<Class<?>, List<?>> getSignals() {
    HashMap<Class<?>, List<?>> signalsCopy = new HashMap<>();
    dataSignals.forEach((k, v) -> signalsCopy.put(k, new ArrayList<>(v)));
    return signalsCopy;
  }

  @Override
  public String toString() {
    return String.format(
        "{%s}",
        dataSignals.entrySet().stream()
            .map(
                e ->
                    String.format(
                        "\"%s\":[%s]",
                        e.getKey().getSimpleName(),
                        e.getValue().stream().map(Object::toString).collect(joining(","))))
            .collect(joining(",")));
  }

  /** Type-checked way of adding data. */
  <T> void addSignal(Class<T> cls, List<T> data) {
    dataSignals.put(cls, data);
    logger.info(String.format("added signal for %s", cls.getSimpleName()));
  }

  JCarbonReport() {}
}
