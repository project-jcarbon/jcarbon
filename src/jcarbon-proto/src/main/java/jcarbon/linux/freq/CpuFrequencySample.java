package jcarbon.linux.freq;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A sample from the cpufreq system that represents the current frequencies ordered by cpu id. */
public final class CpuFrequencySample implements Comparable<CpuFrequencySample> {
  private final Instant timestamp;
  private final ArrayList<CpuFrequency> frequencies = new ArrayList<>();

  CpuFrequencySample(Instant timestamp, Iterable<CpuFrequency> frequencies) {
    this.timestamp = timestamp;
    frequencies.forEach(this.frequencies::add);
  }

  public Instant timestamp() {
    return timestamp;
  }

  public List<CpuFrequency> data() {
    return new ArrayList<>(frequencies);
  }

  @Override
  public int compareTo(CpuFrequencySample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
