package jcarbon.data;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataOperationsTest {
  private static final BiFunction<Integer, Integer, Integer> DIFFERENCE = (i1, i2) -> i2 - i1;

  @Test
  public void forwardApply1() {
    List<Integer> result = DataOperations.forwardApply(List.of(1, 1), DIFFERENCE);

    assertEquals(1, result.size());
    assertEquals(0, result.get(0).intValue());
  }

  @Test
  public void forwardApply2() {
    List<Integer> values = IntStream.range(0, 100).mapToObj(Integer::valueOf).collect(toList());

    List<Integer> result = DataOperations.forwardApply(values, DIFFERENCE);

    assertEquals(99, result.size());
    assertEquals(99, result.stream().mapToInt(i -> i).sum());
  }
}
