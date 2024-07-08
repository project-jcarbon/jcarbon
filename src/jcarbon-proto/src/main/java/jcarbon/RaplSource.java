package jcarbon;

import static jcarbon.util.LoggerUtil.getLogger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import jcarbon.linux.powercap.Powercap;
import jcarbon.linux.powercap.PowercapSample;
import jcarbon.rapl.Rapl;
import jcarbon.rapl.RaplSample;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;
import jcarbon.util.Timestamps;

/** A helper class that automatically picks an available energy source. */
final class RaplSource {
  private static final Logger logger = getLogger();

  private static final RaplSource RAPL =
      new RaplSource("/dev/cpu/<socket>/msr", Rapl::sample);
  private static final RaplSource POWERCAP =
      new RaplSource("/sys/devices/virtual/powercap/intel-rapl", Powercap::sample);
  private static final RaplSource FAKE = createFakeSource();

  /** Grab the first available energy source. Priority is rapl > powercap > fake */
  public static RaplSource getRaplSource() {
    logger.info("checking for a rapl source");
    if (Powercap.isAvailable()) {
      logger.info("found powercap!");
      return POWERCAP;
    } else if (Rapl.isAvailable()) {
      logger.info("found direct rapl access!");
      return RAPL;
    }
    logger.info("no energy source found! resorting to an empty fake");
    return FAKE;
  }

  final String name;
  final Supplier<Optional<?>> source;

  private RaplSource(String name, Supplier<Optional<?>> source) {
    this.name = name;
    this.source = source;
  }

  public SignalInterval difference(Object first, Object second) {
    if (name.equals("/sys/devices/virtual/powercap/intel-rapl")) {
      return Powercap.difference((PowercapSample) first, (PowercapSample) second);
    } else if (name.equals("/dev/cpu/<socket>/msr")) {
      return Rapl.difference((RaplSample) first, (RaplSample) second);
    } else {
      return readingDifference((FakeRaplReading) first, (FakeRaplReading) second);
    }
  }

  private static RaplSource createFakeSource() {
    final AtomicInteger counter = new AtomicInteger(0);
    return new RaplSource(
        "/fake/atomic-counter",
        () -> {
          int value = counter.getAndIncrement();
          Timestamp timestamp = Timestamps.now();
          return Optional.of(new FakeRaplReading(timestamp, value));
        });
  }

  private static SignalInterval readingDifference(FakeRaplReading first, FakeRaplReading second) {
    if (Timestamps.isAfter(first.timestamp, second.timestamp)) {
      throw new IllegalArgumentException(
          String.format(
              "first sample is not before second sample (%s !< %s)",
              first.timestamp, second.timestamp));
    }
    return SignalInterval.newBuilder()
        .setStart(first.timestamp)
        .setEnd(second.timestamp)
        .addData(SignalData.newBuilder().setValue(second.value - first.value))
        .build();
  }

  private static class FakeRaplReading {
    private final Timestamp timestamp;
    private final int value;

    private FakeRaplReading(Timestamp timestamp, int value) {
      this.timestamp = timestamp;
      this.value = value;
    }
  }
}
