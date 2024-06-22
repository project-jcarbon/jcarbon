package jcarbon.cpu.rapl;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Very simple energy monitor that reports energy consumption over 10 millisecond intervals. */
final class RaplMonitor {
  public static void main(String[] args) throws Exception {
    List<RaplEnergy> energy = new ArrayList<>();
    RaplSource source = RaplSource.getRaplSource();

    Instant start = Instant.now();
    RaplSample last = source.sample().get();
    while (true) {
      Thread.sleep(250);
      RaplSample current = source.sample().get();
      RaplEnergy interval = source.difference(last, current);
      String message =
          Arrays.toString(interval.data().stream().mapToDouble(d -> d.value()).toArray());
      System.out.print(message);
      System.out.print(String.join("", Collections.nCopies(message.length(), "\b")));
      energy.add(interval);
      if (interval.data().stream().mapToDouble(d -> d.value()).sum() < 0) {
        Instant end = Instant.now();
        System.out.println();
        System.out.println(String.format("overflow occurred at %s!", Duration.between(start, end)));
        start = end;
      }
      last = current;
    }
  }
}
