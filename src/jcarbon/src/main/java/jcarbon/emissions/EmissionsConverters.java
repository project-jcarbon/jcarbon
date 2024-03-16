
package jcarbon.emissions;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors; 
import java.util.stream.Stream;

import jcarbon.cpu.rapl.RaplReading;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.eflect.TaskEnergy;

import jcarbon.data.Interval;
import jcarbon.emissions.EmissionsInterval;
import jcarbon.emissions.EmissionsConverter;

public final class EmissionsConverters {
    ///private static final String DEFAULT_INTENSITY_FILE = "jRAPL/src/jcarbon/src/resources/emissions/WorldIntensity.csv";
    private static final String DEFAULT_INTENSITY_FILE = "/home/vincent/jCarbon_eevee/jRAPL/src/jcarbon/src/resources/emissions/WorldIntensity.csv";
    private static final Map<String, Double> CARBON_INTENSITY_MAP = getCarbonIntensity();

    public static EmissionsConverter forLocale(String locale){
        return new EmissionsConverter(CARBON_INTENSITY_MAP.get(locale));
    }

    public static Map<String, Double> getCarbonIntensity(){
        //parses the csv, maps into a Map;
        try{
            return Files.readAllLines(Path.of(System.getProperty("jcarbon.mix.emissions", DEFAULT_INTENSITY_FILE)))
                    .stream()
                    .skip(1)
                    .collect(Collectors.toMap(s -> s.split(",")[0], s -> Double.parseDouble(s.split(",")[2])));
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
        
    }


}

