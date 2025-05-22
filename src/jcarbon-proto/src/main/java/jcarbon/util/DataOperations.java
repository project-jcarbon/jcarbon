package jcarbon.util;

import static jcarbon.util.Timestamps.isBefore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import jcarbon.signal.SignalInterval;

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

  /** Applys a method between two {@link SignalInterval} {@link Lists} along the time axis. */
  public static List<SignalInterval> forwardAlign(
      List<SignalInterval> firstData,
      List<SignalInterval> secondData,
      BiFunction<SignalInterval, SignalInterval, SignalInterval> func) {
    return forwardPartialAlign(firstData, secondData, func.andThen(Optional::of));
  }

  /**
   * Attempts to apply a method between two {@link SignalInterval} {@link Lists} along the time
   * axis.
   */
  public static List<SignalInterval> forwardPartialAlign(
      List<SignalInterval> firstData,
      List<SignalInterval> secondData,
      BiFunction<SignalInterval, SignalInterval, Optional<SignalInterval>> func) {
    Iterator<SignalInterval> firstIt = firstData.iterator();
    SignalInterval first = firstIt.next();

    Iterator<SignalInterval> secondIt = secondData.iterator();
    SignalInterval second = secondIt.next();

    ArrayList<SignalInterval> alignedData = new ArrayList<>();
    while (true) {
      // TODO: i am not sufficient convinced this works as intended. i'll do a pretty thorough
      // refactor to make sure the rules i developed for smargadine are implemented
      if (isBefore(first.getEnd(), second.getStart())) {
        if (!firstIt.hasNext()) {
          break;
        }
        first = firstIt.next();
        continue;
      }
      if (isBefore(second.getEnd(), first.getStart())) {
        if (!secondIt.hasNext()) {
          break;
        }
        second = secondIt.next();
        continue;
      }

      func.apply(first, second).ifPresent(alignedData::add);

      if (isBefore(first.getStart(), second.getStart())) {
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
