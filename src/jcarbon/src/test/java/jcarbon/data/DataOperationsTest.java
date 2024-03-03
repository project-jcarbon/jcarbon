package jcarbon.data;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataOperationsTest {
  private static final BiFunction<Integer, Integer, Integer> SUBTRACT = (i1, i2) -> i2 - i1;

  @Test
  public void forwardApply1() {
    List<Integer> result = DataOperations.forwardApply(List.of(1, 1), (i1, i2) -> i2 - i1);

    assertEquals(1, result.size());
    assertEquals(0, result.get(0).intValue());
  }

  @Test
  public void forwardApply2() {
    List<Duration> result =
        DataOperations.forwardApply(
            IntStream.range(0, 10).mapToObj(i -> Instant.ofEpochMilli(i)).collect(toList()),
            Duration::between);

    assertEquals(9, result.size());
    assertEquals(9, result.stream().mapToLong(d -> d.toMillis()).sum());
  }

  @Test
  public void forwardAlign1() {
    List<TestInterval> intervals =
        IntStream.range(0, 10)
            .mapToObj(
                i -> new TestInterval(Instant.ofEpochMilli(i), Instant.ofEpochMilli(i + 1), i))
            .collect(toList());
    List<TestInterval> values =
        DataOperations.forwardAlign(
            intervals,
            intervals,
            (i1, i2) ->
                new TestInterval(
                    TimeOperations.max(i1.start(), i2.start()),
                    TimeOperations.min(i1.end(), i2.end()),
                    i1.data() * i2.data()));

    assertEquals(19, values.size());
    assertEquals(525, values.stream().mapToLong(i -> i.data()).sum());
  }

  private static class TestInterval implements Interval<Long> {
    private final Instant start;
    private final Instant end;
    private final long value;

    private TestInterval(Instant start, Instant end, long value) {
      this.start = start;
      this.end = end;
      this.value = value;
    }

    @Override
    public Instant start() {
      return start;
    }

    @Override
    public Instant end() {
      return end;
    }

    @Override
    public Long data() {
      return Long.valueOf(value);
    }

    @Override
    public String toString() {
      return String.format("%s,%s,%d", start, end, value);
    }
  }
}
