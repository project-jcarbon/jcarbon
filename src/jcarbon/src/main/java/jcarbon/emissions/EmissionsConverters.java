package jcarbon.emissions;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.toMap;
import static jcarbon.util.LoggerUtil.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import jcarbon.util.NativeUtils;

/** A class that creates a carbon intensity map from locale. */
public final class EmissionsConverters {
  private static final Logger logger = getLogger();
  private static final String DEFAULT_INTENSITY_FILE = "/emissions/WorldIntensity.csv";
  private static final double GLOBAL_INTENSITY = 475.0;
  // TODO: find a way to GPS look up locale?
  private static final String DEFAULT_LOCALE =
      System.getProperty("jcarbon.emissions.locale", "USA");
  private static final Map<String, Double> CARBON_INTENSITY_MAP = getCarbonIntensity();

  public static final EmissionsConverter GLOBAL_CONVERTER =
      new EmissionsConverter(GLOBAL_INTENSITY);

  public static EmissionsConverter forLocale(String locale) {
    if (CARBON_INTENSITY_MAP.containsKey(locale)) {
      logger.info(
          String.format(
              "creating converter for locale %s (%.2f gCO2/kWh)",
              locale, CARBON_INTENSITY_MAP.get(locale).doubleValue()));
      return new EmissionsConverter(CARBON_INTENSITY_MAP.get(locale).doubleValue());
    } else {
      logger.info(
          String.format(
              "no carbon intensity found for locale %s. using global intensity (%.2f gCO2/kWh)",
              locale, GLOBAL_INTENSITY));
      return GLOBAL_CONVERTER;
    }
  }

  public static EmissionsConverter forDefaultLocale() {
    return forLocale(DEFAULT_LOCALE);
  }

  private static Map<String, Double> getCarbonIntensity() {
    String filePath = System.getProperty("jcarbon.emissions.intensity");
    if (filePath == null) {
      return getDefaultIntensity();
    }

    Path path = Path.of(filePath);
    if (!Files.exists(path)) {
      logger.info(String.format("locale carbon intensity file %s could not be found", filePath));
      return getDefaultIntensity();
    }

    try {
      return toCsv(Files.readAllLines(path));
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to read %s", filePath), e);
    }
  }

  private static Map<String, Double> getDefaultIntensity() {
    logger.info("retrieving carbon intensity from defaults");
    try {
      return toCsv(NativeUtils.readFileContentsFromJar(DEFAULT_INTENSITY_FILE));
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read the default intensity file.", e);
    }
  }

  /** Parses a csv file with a header like "locale,name,intensity". */
  private static Map<String, Double> toCsv(List<String> lines) {
    return lines.stream()
        .skip(1)
        .collect(toMap(s -> s.split(",")[0], s -> parseDouble(s.split(",")[2])));
  }
}
