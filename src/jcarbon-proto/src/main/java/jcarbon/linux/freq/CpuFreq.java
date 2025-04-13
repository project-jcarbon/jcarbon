package jcarbon.linux.freq;

import static java.util.stream.Collectors.toMap;
import static jcarbon.util.Timestamps.fromInstant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;

/**
 * A simple (unsafe) wrapper for reading the dvfs system. Consult
 * https://www.kernel.org/doc/html/v4.14/admin-guide/pm/cpufreq.html for more details.
 */
public final class CpuFreq {
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final Path SYS_CPU = Paths.get("/sys", "devices", "system", "cpu");

  /** Returns the expected frequency in KHz of a cpu. */
  public static int getFrequency(int cpu) {
    return readCounter(cpu, "cpuinfo_cur_freq");
  }

  /** Returns the observed frequency in Hz of a cpu. */
  public static int getObservedFrequency(int cpu) {
    return 1000 * readCounter(cpu, "scaling_cur_freq");
  }

  /** Returns the current governor of a cpu. */
  public static String getGovernor(int cpu) {
    return readFromComponent(cpu, "scaling_governor");
  }

  public static CpuFrequencySample sample() {
    Instant timestamp = Instant.now();
    ArrayList<CpuFrequency> readings = new ArrayList<>();
    for (int cpu = 0; cpu < CPU_COUNT; cpu++) {
      readings.add(
          new CpuFrequency(cpu, getGovernor(cpu), getObservedFrequency(cpu), getFrequency(cpu)));
    }
    return new CpuFrequencySample(timestamp, readings);
  }

  public static List<SignalData> between(List<CpuFrequency> first, List<CpuFrequency> second) {
    Map<Integer, CpuFrequency> secondMap = second.stream().collect(toMap(r -> r.cpu, r -> r));
    ArrayList<SignalData> frequencies = new ArrayList<>();
    for (CpuFrequency reading : first) {
      if (secondMap.containsKey(reading.cpu)) {
        CpuFrequency other = secondMap.get(reading.cpu);
        frequencies.add(
            SignalData.newBuilder()
                .addMetadata(
                    SignalData.Metadata.newBuilder()
                        .setName("cpu")
                        .setValue(Integer.toString(reading.cpu)))
                .setValue(reading.frequency)
                .build());
        frequencies.add(
            SignalData.newBuilder()
                .addMetadata(
                    SignalData.Metadata.newBuilder()
                        .setName("cpu")
                        .setValue(Integer.toString(reading.cpu)))
                .setValue(reading.setFrequency)
                .build());
      }
    }
    return frequencies;
  }

  public static SignalInterval difference(CpuFrequencySample first, CpuFrequencySample second) {
    return SignalInterval.newBuilder()
        .setStart(fromInstant(first.timestamp()))
        .setEnd(fromInstant(second.timestamp()))
        .addAllData(between(first.data(), second.data()))
        .build();
  }

  private static int readCounter(int cpu, String component) {
    String counter = readFromComponent(cpu, component);
    if (counter.isBlank()) {
      return 0;
    }
    return Integer.parseInt(counter);
  }

  private static synchronized String readFromComponent(int cpu, String component) {
    try {
      return Files.readString(getComponentPath(cpu, component));
    } catch (Exception e) {
      // e.printStackTrace();
      return "";
    }
  }

  private static Path getComponentPath(int cpu, String component) {
    return Paths.get(SYS_CPU.toString(), String.format("cpu%d", cpu), "cpufreq", component);
  }

  private CpuFreq() {}
}
