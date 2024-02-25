package jcarbon.cpu.freq;

/** A reading from a rapl energy system. */
public final class CpuFrequencyReading {
  // TODO: immutable data structures are "safe" as public
  public final int cpuId;
  public final String governor;
  public final int frequency;
  public final int setFrequency;

  CpuFrequencyReading(int cpuId, String governor, int frequency) {
    this.cpuId = cpuId;
    this.governor = governor;
    this.frequency = frequency;
    this.setFrequency = -1;
  }

  CpuFrequencyReading(int cpuId, String governor, int frequency, int setFrequency) {
    this.cpuId = cpuId;
    this.governor = governor;
    this.frequency = frequency;
    this.setFrequency = setFrequency;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format(
        "{\"cpu_id\":%d,\"governor\":%s,\"frequency\":%d,\"set_frequency\":%d}",
        cpuId, governor, frequency, setFrequency);
  }
}
