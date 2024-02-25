package jcarbon.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class DataOperations {
  /** Applys a method between pairs of adjacent values in ascending order. */
  public static <T, U> List<U> forwardApply(List<T> data, BiFunction<T, T, U> difference) {
    ArrayList<U> diffs = new ArrayList<>();
    for (int i = 0; i < data.size() - 1; i++) {
      diffs.add(difference.apply(data.get(i), data.get(i + 1)));
    }
    return diffs;
  }

  private DataOperations() {}
}
