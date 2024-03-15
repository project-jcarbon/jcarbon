
public final class CarbonIntensity {
    private static final String DEFAULT_INTENSITY_FILE = "jRAPL/src/jcarbon/src/main/resources/emissions/custom_emissions.csv";
    private final EnumMap<CarbonSource, Double> CARBON_INTENSITY_MAP = createMixMap();

    public static CarbonIntensity forLocale(String locale){
        return EmissionsConverter(CARBON_INTENSITY_MAP.get(locale));
    }

    public static EnumMap<CarbonSource, Double> fromCSV(String filepath){
        //parses the csv, maps into a EnumMap
        return Files.readAllLines(Path.of(filepath))
                    .stream()
                    .skip(1)
                    .collect(Collectors.toMap(s -> s.split(",")[0], s -> Double.parseDouble(s.split(",")[2])));
    }

    public static CarbonIntensity createMixMap() {
        //user can create a custom intensity map by not including a mix file
        return fromCsv(System.getProperty("jcarbon.emissions.mix", DEFAULT_INTENSITY_FILE);
    }

}

