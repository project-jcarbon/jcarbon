package jcarbon.emissions;

import static jcarbon.util.LoggerUtil.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** A class that creates a carbon intensity map from locale. */
public final class EmissionsConverters {
  private static final Logger logger = getLogger();
  private static final String DEFAULT_INTENSITY_FILE =
      System.getProperty("user.dir")
          + "/src/jcarbon/src/main/resources/emissions/WorldIntensity.csv";
  private static final double GLOBAL_INTENSITY = 475.0;

  public static final EmissionsConverter GLOBAL_CONVERTER =
      new EmissionsConverter(GLOBAL_INTENSITY);
  private static final Map<String, Double> CARBON_INTENSITY_MAP = getCarbonIntensity();

  public static EmissionsConverter forLocale(String locale) {
    if (CARBON_INTENSITY_MAP.containsKey(locale)) {
      return new EmissionsConverter(CARBON_INTENSITY_MAP.get(locale).doubleValue());
    } else {
      return GLOBAL_CONVERTER;
    }
  }

  private static Map<String, Double> getCarbonIntensity() {
    // parses the csv, maps into a Map;
    Path filepath =
        Path.of(System.getProperty("jcarbon.emissions.intensity", DEFAULT_INTENSITY_FILE));
    logger.info(String.format("Retrieving carbon intensity from %s", filepath));
    if (!Files.exists(filepath)) {
      logger.info(String.format("Locale carbon intensity file %s could not be found", filepath));
      return Map.of();
    }
    try {
      return Files.readAllLines(filepath).stream()
          .skip(1)
          .collect(
              Collectors.toMap(s -> s.split(",")[0], s -> Double.parseDouble(s.split(",")[2])));
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to read %s", filepath), e);
    }
  }
}
