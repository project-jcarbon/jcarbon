package jcarbon.emissions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors; 
import java.util.stream.Stream;

import jcarbon.cpu.rapl.RaplReading;
import jcarbon.cpu.rapl.RaplInterval;
import jcarbon.cpu.eflect.EnergyFootprint;

import jcarbon.data.Interval;
// import jrapl.EnergyInterval;
// import jrapl.EnergyReading;
// import java.time.Instant;
// import jrapl.MicroArchitecture;
import jcarbon.emissions.EmissionsInterval;

public final class EmissionsConverter {
    // private static final String DEFAULT_MIX = System.getProperty("user.dir") + "/src/jrapl/src/main/java/jrapl/emissions/WorldIntensity.csv";
    // public static final String LOCALE =  Locale.getDefault().getISO3Country();
    private static final double JOULE_TO_KWH = 2.77778e-7;
    // private final EnumMap<CarbonSource, Double> carbonMix;

    public EmissionConverter(double carbonIntensity){
        this.carbonIntensity = carbonIntensity;
    }

    public <T extends Interval<?>> EmissionsInterval convert (T interval){
        double emissions = 0;
        if(interval instanceof RaplInterval){
            emissions = convertRaplInterval((RaplInterval) interval);
        }
        else if (interval instanceof EnergyFootprint){
            emissions = convertEnergyFootprint((EnergyFootprint) interval);
        }
        return new EmissionsInterval(interval.start(), interval.end(), emissions);
    }

    public double convertRaplInterval(RaplInterval interval){
        RaplReading[] readings = interval.data();
        double joules = 0;
        for(RaplReading e : readings){
            joules = e.total;
        }
        return new convertJoules(joules);
    }

    public double convertEnergyFootprint(EnergyFootprint interval){
        List<TaskEnergy> readings = interval.data();
        double joules = 0;
        for(List<TaskEnergy> e: readings){
            joules+=e.energy;
        }
        return new convertJoules(joules);
    }

    public double convertJoules(double joules){
        return carbonIntensity * joules * JOULE_TO_KWH;
    }

}
