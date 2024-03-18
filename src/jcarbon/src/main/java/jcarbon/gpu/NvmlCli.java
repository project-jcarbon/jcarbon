package jcarbon.gpu;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

public final class NvmlCli {
  public static List<GpuSample> readFromCli(int periodMillis)
      throws IOException, InterruptedException {
    Process process =
        new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=index,timestamp,power.draw",
                "--format=csv",
                String.format("-lms %d", periodMillis))
            .start();
    process.waitFor();
    return process
        .inputReader()
        .lines()
        .skip(1)
        .map(line -> line.split(","))
        .collect(groupingBy(line -> ZonedDateTime.parse(line[1]).toInstant()))
        .entrySet()
        .stream()
        .map(
            e ->
                new GpuSample(
                    e.getKey(),
                    e.getValue().stream()
                        .map(
                            line ->
                                new GpuReading[] {
                                  new GpuReading(
                                      Integer.parseInt(line[0]), Integer.parseInt(line[2]))
                                })
                        .toArray(GpuReading[]::new)))
        .collect(toList());
  }

  private NvmlCli() {}
}
