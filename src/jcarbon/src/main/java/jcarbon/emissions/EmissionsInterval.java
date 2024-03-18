package jcarbon.emissions;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import jcarbon.data.Interval;

public final class EmissionsInterval implements Interval<Double> {
    private final Instant start;
    private final Instant end;
    private final double emissions;

    public EmissionsInterval(Instant start, Instant end, double emis_total){
        this.start = start;
        this.end = end;
        this.emissions = emissions;
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
    public Double data(){
        return Double.valueOf(emissions);
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
            emissions);
    }
}
