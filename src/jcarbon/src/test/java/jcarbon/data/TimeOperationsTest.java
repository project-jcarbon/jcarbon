package jcarbon.data;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TimeOperationsTest {
  @Test
  public void max() {
    assertEquals(Instant.EPOCH, TimeOperations.max(Instant.EPOCH, Instant.EPOCH));

    assertEquals(Instant.MAX, TimeOperations.max(Instant.EPOCH, Instant.MAX));
    assertEquals(Instant.EPOCH, TimeOperations.max(Instant.EPOCH, Instant.MIN));

    assertEquals(Instant.EPOCH, TimeOperations.max(Instant.MIN, Instant.EPOCH));
    assertEquals(Instant.MAX, TimeOperations.max(Instant.MAX, Instant.EPOCH));

    assertEquals(Instant.MAX, TimeOperations.max(Instant.MIN, Instant.MAX));
    assertEquals(Instant.MAX, TimeOperations.max(Instant.MAX, Instant.MIN));

    assertEquals(Instant.MAX, TimeOperations.max(Instant.MIN, Instant.EPOCH, Instant.MAX));
  }

  @Test
  public void min() {
    assertEquals(Instant.EPOCH, TimeOperations.min(Instant.EPOCH, Instant.EPOCH));

    assertEquals(Instant.EPOCH, TimeOperations.min(Instant.EPOCH, Instant.MAX));
    assertEquals(Instant.MIN, TimeOperations.min(Instant.EPOCH, Instant.MIN));

    assertEquals(Instant.MIN, TimeOperations.min(Instant.MIN, Instant.EPOCH));
    assertEquals(Instant.EPOCH, TimeOperations.min(Instant.MAX, Instant.EPOCH));

    assertEquals(Instant.MIN, TimeOperations.min(Instant.MIN, Instant.MAX));
    assertEquals(Instant.MIN, TimeOperations.min(Instant.MAX, Instant.MIN));

    assertEquals(Instant.MIN, TimeOperations.min(Instant.MIN, Instant.EPOCH, Instant.MAX));
  }

  @Test
  public void divide() {
    assertEquals(1.0 / 2, TimeOperations.divide(Duration.ofMillis(1), Duration.ofMillis(2)), 0.0);
    assertEquals(
        1.0 / 1000, TimeOperations.divide(Duration.ofMillis(1), Duration.ofSeconds(1)), 0.0);
    assertEquals(
        1.0 / 1001,
        TimeOperations.divide(Duration.ofMillis(1), Duration.ofSeconds(1, 1000000)),
        0.0);
  }
}
