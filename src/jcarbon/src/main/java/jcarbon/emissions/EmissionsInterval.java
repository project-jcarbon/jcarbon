package jcarbon.emissions;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.util.Arrays;
import jcarbon.data.Interval;
import jcarbon.cpu.rapl.RaplReading;
import jcarbon.cpu.eflect.TaskEnergy;


public final class EmissionsInterval {
    private final Instant start;
    private final Instant end;

    public EmissionsInterval(Instant start, Instant end, double emis_total){
        this.start = start;
        this.end = end;

        this.emis_total = emis_total;
    }

    @Override
    public Instant start() {
        return start;
    }

    @Override
    public Instant end() {
        return end;
    }

    @Override
    public double getEmissions(){
        return emis_total;
    }

    @Override
    public String toString() {
        // TODO: temporarily using json
        return String.format(
            "{\"start\":{\"seconds\":%d,\"nanos\":%d},\"end\":{\"seconds\":%d,\"nanos\":%d},\"Total Emissions\":[%s]}",
            start.getEpochSecond(),
            start.getNano(),
            end.getEpochSecond(),
            end.getNano(),
            EmissionsInterval.getEmissions());
    }
}
