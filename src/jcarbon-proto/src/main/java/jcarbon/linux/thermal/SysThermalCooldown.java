package jcarbon.linux.thermal;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

final class SysThermalCooldown {
  private static class CooldownArgs {
    private final int periodMillis;
    private final int targetTemperature;

    private CooldownArgs(int periodMillis, int targetTemperature) {
      this.periodMillis = periodMillis;
      this.targetTemperature = targetTemperature;
    }
  }

  private static final Integer DEFAULT_PERIOD_MILLIS = 10000;

  private static CooldownArgs getCooldownArgs(String[] args) throws Exception {
    Option periodOption =
        Option.builder("p")
            .hasArg(true)
            .longOpt("period")
            .desc("period in milliseconds to sample at")
            .type(Integer.class)
            .build();
    Option temperatureOption =
        Option.builder("t")
            .hasArg(true)
            .longOpt("temperature")
            .desc("the target temperature in celsius")
            .type(Integer.class)
            .build();
    Options options = new Options().addOption(periodOption).addOption(temperatureOption);
    CommandLine cmd = new DefaultParser().parse(options, args);
    return new CooldownArgs(
        cmd.getParsedOptionValue(periodOption, DEFAULT_PERIOD_MILLIS).intValue(),
        cmd.getParsedOptionValue(temperatureOption, 35).intValue());
  }

  private static int[] findX86ThermalZones() {
    return getThermalZonesByType("x86_pkg_temp");
  }

  private static int[] getThermalZonesByType(String type) {
    ArrayList<Integer> zones = new ArrayList<>();
    IntStream.range(0, SysThermal.getZoneCount())
        .forEach(
            zone -> {
              if (SysThermal.getZoneType(zone).equals(type)) {
                zones.add(zone);
              }
            });
    return zones.stream().mapToInt(Integer::intValue).toArray();
  }

  private static void cooldown(CooldownArgs args) throws Exception {
    int period = args.periodMillis;
    int temperature = args.targetTemperature;

    int k = 10;
    int p = Double.valueOf(0.80 * k).intValue();

    int[] zones = findX86ThermalZones();
    Map<Integer, ArrayList<Integer>> samples =
        Arrays.stream(zones)
            .mapToObj(Integer::valueOf)
            .collect(toMap(z -> z, z -> new ArrayList<>()));

    Instant start = Instant.now();
    while (true) {
      HashMap<Integer, Double> temps = new HashMap<>();
      HashMap<Integer, Boolean> met = new HashMap<>();
      int sampleCount = 0;
      for (int zone : zones) {
        ArrayList<Integer> s = samples.get(zone);
        s.add(SysThermal.getTemperature(zone));
        if (s.size() > k) {
          s.remove(0);
        }
        sampleCount = s.size();
        temps.put(zone, s.stream().mapToInt(i -> i).average().getAsDouble());
        met.put(
            zone, sampleCount == k && s.stream().filter(temp -> temp <= temperature).count() >= p);
      }
      String status =
          Arrays.stream(zones)
              .mapToObj(
                  zone ->
                      String.format(
                          "%d->%f C (%s)", zone, temps.get(zone).doubleValue(), met.get(zone)))
              .collect(joining(", "));
      String message = String.format("zone status (%d/%d samples): %s", sampleCount, k, status);
      System.out.print(message);
      System.out.print("\b".repeat(message.length()));
      if (met.values().stream().allMatch(b -> b == true)) {
        break;
      }
      Thread.sleep(period);
    }

    Instant end = Instant.now();
    Duration elapsed = Duration.between(start, end);
    System.out.println(
        String.format("cooled down to %d C in %s%s", temperature, elapsed, " ".repeat(50)));
  }

  public static void main(String[] args) throws Exception {
    cooldown(getCooldownArgs(args));
  }
}
