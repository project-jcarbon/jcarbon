package jcarbon.gpu;

import static org.bytedeco.javacpp.nvml.nvmlDeviceGetTotalEnergyConsumption;
import static org.bytedeco.javacpp.nvml.nvmlUnitGetCount;
import static org.bytedeco.javacpp.nvml.nvmlUnitGetDevices;
import static org.bytedeco.javacpp.nvml.nvmlUnitGetHandleByIndex;

import java.time.Instant;
import org.bytedeco.javacpp.nvml.nvmlDevice_st;
import org.bytedeco.javacpp.nvml.nvmlUnit_st;

public final class Nvml {
  public static Nvml get() {
    int[] count = new int[1];
    nvmlUnitGetCount(count);
    nvmlDevice_st[] devices = new nvmlDevice_st[count[0]];
    for (int i = 0; i < count[0]; i++) {
      nvmlUnit_st unit = new nvmlUnit_st();
      nvmlUnitGetHandleByIndex(i, unit);
      nvmlDevice_st device = new nvmlDevice_st();
      nvmlUnitGetDevices(unit, count, device);
      devices[i] = device;
    }
    return new Nvml(devices);
  }

  private final nvmlDevice_st[] devices;

  private Nvml(nvmlDevice_st[] devices) {
    this.devices = devices;
  }

  public GpuSample sample() {
    Instant now = Instant.now();
    GpuReading[] readings = new GpuReading[devices.length];
    for (int i = 0; i < devices.length; i++) {
      long[] energy = new long[1];
      nvmlDeviceGetTotalEnergyConsumption(devices[i], energy);
      readings[i] = new GpuReading(i, energy[0]);
    }
    return new GpuSample(now, readings);
  }

  public static void main(String[] args) {
    System.out.println(Nvml.get().sample());
  }
}
