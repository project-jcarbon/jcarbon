package jcarbon.gpu;

public final class GpuReading {
  // TODO: immutable data structures are "safe" as public
  public final int deviceId;
  public final long energy;

  GpuReading(int deviceId, long energy) {
    this.deviceId = deviceId;
    this.energy = energy;
  }

  @Override
  public String toString() {
    // TODO: temporarily using json
    return String.format("{\"device_id\":%d,\"energy\":%s}", deviceId, energy);
  }
}
