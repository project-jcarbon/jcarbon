package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.util.DataOperations.forwardApply;
import static jcarbon.util.DataOperations.forwardPartialAlign;

import java.lang.StackWalker.Option;
import java.nio.file.SecureDirectoryStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.LocaleEmissionsConverters;
import jcarbon.linux.freq.CpuFreq;
import jcarbon.linux.freq.CpuFrequencySample;
import jcarbon.linux.jiffies.EflectAccounting;
import jcarbon.linux.jiffies.JiffiesAccounting;
import jcarbon.linux.jiffies.ProcStat;
import jcarbon.linux.jiffies.ProcTask;
import jcarbon.linux.jiffies.ProcessSample;
import jcarbon.linux.jiffies.SystemSample;
import jcarbon.linux.thermal.SysThermal;
import jcarbon.linux.thermal.ThermalZonesSample;
import jcarbon.signal.Component;
import jcarbon.signal.Report;
import jcarbon.signal.Signal;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;
import jcarbon.util.LoggerUtil;
import jcarbon.util.Timestamps;

/** A class to collect and provide jcarbon signals for single sampling. */
public final class JCarbonEndToEndMonitor implements JCarbon {
  private static final Logger logger = LoggerUtil.getLogger();

  private static final String OS_NAME = System.getProperty("os.name", "unknown");
  private static final String PROC_STAT = "/proc/stat";

  // TODO: do we need to wire this back in?
  private final RaplSource raplSource = RaplSource.getRaplSource();
  private final EmissionsConverter converter = LocaleEmissionsConverters.forDefaultLocale();

  private MonotonicTimeSample monotonicTimeStart;
  private MonotonicTimeSample monotonicTimeEnd;
  private SystemSample systemStart;
  private SystemSample systemEnd;
  private Optional<?> raplStart;
  private Optional<?> raplEnd;
    
  /** Starts the sampling. */
  @Override
  public void start() {
    logger.info("starting jcarbon");

    monotonicTimeStart = new MonotonicTimeSample();
    systemStart = ProcStat.sampleCpus();
    raplStart = raplSource.source.get();
  }

  /**
   * Stops the sampling and merges the data they collected into a {@link JCarbonReport}.
   * Returns an empty {@link Optional} if jcarbon wasn't running.
   */
  @Override
  public Optional<Report> stop() {
    logger.info("stopping jcarbon");
    
    monotonicTimeEnd = new MonotonicTimeSample();
    systemEnd = ProcStat.sampleCpus();
    raplEnd = raplSource.source.get();

    Component.Builder systemComponent =
        Component.newBuilder().setComponentType("linux_system").setComponentId(OS_NAME);

    // physical signals
    logger.info("creating monotonic time signal");
    createPhysicalSignal(
            forwardApply(
                List.of(monotonicTimeStart, monotonicTimeEnd), JCarbonEndToEndMonitor::monotonicTimeDifference),
            Signal.Unit.NANOSECONDS,
            "clock_gettime(CLOCK_MONOTONIC, &ts)")
        .ifPresent(systemComponent::addSignal);

    Optional<Signal> raplEnergy =
    createPhysicalSignal(
        forwardApply(
            List.of(raplStart, raplEnd).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList()),
            raplSource::difference),
        Signal.Unit.JOULES,
        raplSource.name);
    raplEnergy.ifPresent(systemComponent::addSignal);
    logger.info("creating system jiffies signal");

    Optional<Signal> systemJiffies =
        createPhysicalSignal(
            forwardApply(List.of(systemStart, systemEnd), ProcStat::between),
            Signal.Unit.JIFFIES,
            PROC_STAT);
    systemJiffies.ifPresent(systemComponent::addSignal);

    // virtual signals
    if (raplEnergy.isEmpty()) {
      logger.info("not creating rapl emissions: no rapl energy");
    } else {
      logger.info("creating rapl emissions signal");
      systemComponent.addSignal(convertToEmissions(raplEnergy.get()));
    }

    Report.Builder report = Report.newBuilder();
    if (systemComponent.getSignalCount() > 0) {
      report.addComponent(systemComponent);
    }
    if (report.getComponentCount() > 0) {
      return Optional.of(report.build());
    }
    return Optional.empty();
  }

  @Override
  public Signal convertToEmissions(Signal signal) {
    return converter.convert(signal);
  }

  private Optional<Signal> createPhysicalSignal(
      List<SignalInterval> intervals, Signal.Unit unit, String source) {
    if (intervals.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        Signal.newBuilder().setUnit(unit).addSource(source).addAllInterval(intervals).build());
  }

  private static class MonotonicTimeSample {
    private final Timestamp timestamp;
    private final Timestamp monotonicTime;

    private MonotonicTimeSample() {
      this.timestamp = Timestamps.now();
      this.monotonicTime = Timestamps.monotonicTime();
    }
  }

  private static SignalInterval monotonicTimeDifference(
      MonotonicTimeSample first, MonotonicTimeSample second) {
    return SignalInterval.newBuilder()
        .setStart(first.timestamp)
        .setEnd(second.timestamp)
        .addData(
            SignalData.newBuilder()
                .setValue(
                    (double)
                        (1000000000 * first.monotonicTime.getSecs()
                            + first.monotonicTime.getNanos())))
        .build();
  }
}