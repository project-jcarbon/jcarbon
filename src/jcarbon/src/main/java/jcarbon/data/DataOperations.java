package jcarbon.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/** Utilities for complicated data operations across linear data. */
public final class DataOperations {
  /** Applys a method between pairs of adjacent values in ascending order. */
  public static <T, U> List<U> forwardApply(List<T> data, BiFunction<T, T, U> func) {
    if (data.size() < 2) {
      return List.of();
    }
    ArrayList<U> diffs = new ArrayList<>();
    for (int i = 0; i < data.size() - 1; i++) {
      diffs.add(func.apply(data.get(i), data.get(i + 1)));
    }
    return diffs;
  }

  /** Applys a method between two {@link Interval} {@link Lists} along the time axis. */
  public static <T extends Interval<?>, U extends Interval<?>, V> List<V> forwardAlign(
      List<T> firstData, List<U> secondData, BiFunction<T, U, V> func) {
    return forwardPartialAlign(firstData, secondData, func.andThen(Optional::of));
  }

  /** Attempts to apply a method between two {@link Interval} {@link Lists} along the time axis. */
  public static <T extends Interval<?>, U extends Interval<?>, V> List<V> forwardPartialAlign(
      List<T> firstData, List<U> secondData, BiFunction<T, U, Optional<V>> func) {
    Iterator<T> firstIt = firstData.iterator();
    T first = firstIt.next();

    Iterator<U> secondIt = secondData.iterator();
    U second = secondIt.next();

    ArrayList<V> alignedData = new ArrayList<>();
    while (true) {
      // TODO: i am not sufficient convinced this works as intended. i'll do a pretty thorough
      // refactor to make sure the rules i developed for smargadine are implemented
      if (first.end().isBefore(second.start())) {
        if (!firstIt.hasNext()) {
          break;
        }
        first = firstIt.next();
        continue;
      }
      if (second.end().isBefore(first.start())) {
        if (!secondIt.hasNext()) {
          break;
        }
        second = secondIt.next();
        continue;
      }

      func.apply(first, second).ifPresent(alignedData::add);

      if (first.start().isBefore(second.start())) {
        if (!firstIt.hasNext()) {
          break;
        }
        first = firstIt.next();
      } else {
        if (!secondIt.hasNext()) {
          break;
        }
        second = secondIt.next();
      }
    }
    return alignedData;
  }

  private DataOperations() {}
}
