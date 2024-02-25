package jcarbon.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public final class DataOperations {
  /** Applys a method between pairs of adjacent values in ascending order. */
  public static <T, U> List<U> forwardApply(List<T> data, BiFunction<T, T, U> func) {
    ArrayList<U> diffs = new ArrayList<>();
    for (int i = 0; i < data.size() - 1; i++) {
      diffs.add(func.apply(data.get(i), data.get(i + 1)));
    }
    return diffs;
  }

  /** Applys a method between pairs of intervals along their time axis. */
  public static <T extends Interval<?>, U extends Interval<?>, V> List<V> forwardAlign(
      List<T> firstData, List<U> secondData, BiFunction<T, U, Optional<V>> func) {
    Iterator<T> firstIt = firstData.iterator();
    T first = firstIt.next();

    Iterator<U> secondIt = secondData.iterator();
    U second = secondIt.next();

    ArrayList<V> alignedData = new ArrayList<>();
    while (true) {
      // TODO: i am not sufficient convinced this works as intended. i'll do a pretty thorough
      // refactor to make sure the rules i developed for smargadine are implemented
      // TODO: there needs to be a better way to check if intervals overlap.
      if (TimeOperations.between(first.end(), second.start(), second.end())
          == TimeOperations.Region.BEFORE) {
        if (!firstIt.hasNext()) {
          break;
        }
        first = firstIt.next();
        continue;
      }
      if (TimeOperations.between(second.end(), first.start(), first.end())
          == TimeOperations.Region.BEFORE) {
        if (!secondIt.hasNext()) {
          break;
        }
        second = secondIt.next();
        continue;
      }

      func.apply(first, second).ifPresent(alignedData::add);

      if (TimeOperations.lessThan(first.start(), second.start())) {
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
