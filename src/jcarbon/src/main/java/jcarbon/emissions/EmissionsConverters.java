
package jcarbon.emissions;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors; 
import java.util.stream.Stream;
import java.lang.Integer; 

import jcarbon.cpu.rapl.RaplReading;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.eflect.TaskEnergy;

import jcarbon.data.Interval;
import jcarbon.emissions.EmissionsInterval;
import jcarbon.emissions.EmissionsConverter;

/** A class that creates a carbon intensity map from locale. */
public final class EmissionsConverters {
    private static final String DEFAULT_INTENSITY_FILE = System.getProperty("user.dir") + "/jRAPL/src/jcarbon/src/main/resources/emissions/WorldIntensity.csv";
    private static final double GLOBAL_INTENSITY = 475.0;

    public static final EmissionsConverter GLOBAL_CONVERTER = new EmissionsConverter(GLOBAL_INTENSITY);
    private static final Map<String, Double> CARBON_INTENSITY_MAP = getCarbonIntensity();

    public static EmissionsConverter forLocale(String locale){
        if(CARBON_INTENSITY_MAP.containsKey(locale)){
            return new EmissionsConverter(CARBON_INTENSITY_MAP.get(locale).doubleValue());
        }
        else{
            return GLOBAL_CONVERTER;
        }
        
    }

    private static Map<String, Double> getCarbonIntensity(){
        //parses the csv, maps into a Map;
        Path filepath = Path.of(System.getProperty("jcarbon.emissions.intensity", DEFAULT_INTENSITY_FILE));
        if(!Files.exists(filepath)){
            return Map.of();
        }
        try {
            return Files.readAllLines(filepath)
                    .stream()
                    .skip(1)
                    .collect(Collectors.toMap(s -> s.split(",")[0], s -> Double.parseDouble(s.split(",")[2])));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read %s", filepath), e);
        }
        
    }

}

