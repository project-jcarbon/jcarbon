package jcarbon.linux.freq;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jcarbon.cpu.LinuxComponents;
import jcarbon.data.Sample;
import jcarbon.data.Unit;

/** A sample from the cpufreq system that represents the current frequencies ordered by cpu id. */
public final class CpuFrequencySample
    implements Sample<CpuFrequency>, Comparable<CpuFrequencySample> {
  private final Instant timestamp;
  private final ArrayList<CpuFrequency> frequencies = new ArrayList<>();

  CpuFrequencySample(Instant timestamp, Iterable<CpuFrequency> frequencies) {
    this.timestamp = timestamp;
    frequencies.forEach(this.frequencies::add);
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public String component() {
    return LinuxComponents.OS_COMPONENT;
  }

  @Override
  public Unit unit() {
    return Unit.MEGAHERTZ;
  }

  @Override
  public List<CpuFrequency> data() {
    return new ArrayList<>(frequencies);
  }

  @Override
  public String toString() {
    return toJson();
  }

  @Override
  public int compareTo(CpuFrequencySample other) {
    return timestamp().compareTo(other.timestamp());
  }
}
