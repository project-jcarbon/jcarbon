package jcarbon.linux.thermal;

import static jcarbon.util.LoggerUtil.getLogger;

import java.util.logging.Logger;

/**
 * Very simple temperature monitor that reports system temperature over 10 millisecond intervals.
 */
final class TemperatureMonitor {
  private static final Logger logger = getLogger();

  public static void main(String[] args) throws Exception {
    int zone = Integer.parseInt(args[0]);
    int temperature = Integer.parseInt(args[1]);
    int sample_temp = 0;
    long previousTime = System.currentTimeMillis();

    while (sample_temp == 0 || temperature < sample_temp) {
      sample_temp = SysThermal.getTemperature(0);
      logger.info(String.format("Temperature for thermal zone %s is at %s", zone, sample_temp));
      Thread.sleep(10);
    }

    long currentTime = System.currentTimeMillis();
    double elapsed = (currentTime - previousTime) / 1000.0;
    logger.info(String.format("Cooling down to %s Celsius took %s seconds", temperature, elapsed));
  }
}
