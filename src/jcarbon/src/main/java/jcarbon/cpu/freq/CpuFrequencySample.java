package jcarbon.cpu.freq;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Sample;

/** A sample from the cpufreq system that represents the current frequencies ordered by cpu id. */
public final class CpuFrequencySample
    implements Sample<CpuFrequencyReading[]>, Comparable<CpuFrequencySample> {
  private final Instant timestamp;
  private final CpuFrequencyReading[] frequencies;

  CpuFrequencySample(Instant timestamp, CpuFrequencyReading[] frequencies) {
    this.timestamp = timestamp;
    this.frequencies = Arrays.copyOf(frequencies, frequencies.length);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public CpuFrequencyReading[] data() {
    return Arrays.copyOf(frequencies, frequencies.length);
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"timestamp\":{\"seconds\":%d,\"nanos\":%d},\"frequencies\":[%s]}",
        timestamp.getEpochSecond(),
        timestamp.getNano(),
        Arrays.stream(frequencies).map(CpuFrequencyReading::toString).collect(joining(",")));
  }

  @Override
  public int compareTo(CpuFrequencySample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
