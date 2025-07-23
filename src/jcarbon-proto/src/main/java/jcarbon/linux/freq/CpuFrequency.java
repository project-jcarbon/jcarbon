package jcarbon.linux.freq;

/** A reading from a rapl energy system. */
public final class CpuFrequency {
  // TODO: immutable data structures are "safe" as public
  public final int cpu;
  public final String governor;
  public final long frequency;
  public final long setFrequency;

  CpuFrequency(int cpu, String governor, long frequency, long setFrequency) {
    this.cpu = cpu;
    this.governor = governor;
    this.frequency = frequency;
    this.setFrequency = setFrequency;
  }
}
