package jcarbon.cpu.rapl;

import static jcarbon.util.LoggerUtil.getLogger;

import java.util.logging.Logger;

/** Very simple energy monitor that reports energy consumption over 10 millisecond intervals. */
final class RaplMonitor {
  private static final Logger logger = getLogger();

  public static void main(String[] args) throws Exception {
    RaplSource source = RaplSource.getRaplSource();

    RaplSample last = source.sample().get();
    while (true) {
      Thread.sleep(10);
      RaplSample current = source.sample().get();
      RaplEnergy interval = source.difference(last, current);
      logger.info(String.format("%s", interval));
      last = current;
    }
  }
}
