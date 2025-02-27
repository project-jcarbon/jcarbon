package jcarbon.linux.freq;

import jcarbon.linux.LinuxComponents;
import jcarbon.data.Data;

/** A reading from a rapl energy system. */
public final class CpuFrequency implements Data {
  // TODO: immutable data structures are "safe" as public
  public final int cpu;
  public final String governor;
  public final int frequency;
  public final int setFrequency;

  private final String component;

  CpuFrequency(int cpu, String governor, int frequency, int setFrequency) {
    this.cpu = cpu;
    this.governor = governor;
    this.frequency = frequency;
    this.setFrequency = setFrequency;
    this.component = LinuxComponents.cpuComponent(cpu);
  }

  @Override
  public String component() {
    return component;
  }

  @Override
  public double value() {
    return frequency;
  }

  @Override
  public String toString() {
    return toJson();
  }
}
