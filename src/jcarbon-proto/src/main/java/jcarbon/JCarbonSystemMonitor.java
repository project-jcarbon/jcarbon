package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.util.DataOperations.forwardApply;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.LocaleEmissionsConverters;
import jcarbon.linux.jiffies.ProcStat;
import jcarbon.linux.jiffies.SystemSample;
import jcarbon.signal.Component;
import jcarbon.signal.Report;
import jcarbon.signal.Signal;
import jcarbon.signal.SignalInterval;
import jcarbon.signal.SignalInterval.SignalData;
import jcarbon.signal.SignalInterval.Timestamp;
import jcarbon.util.LoggerUtil;
import jcarbon.util.SamplingFuture;
import jcarbon.util.Timestamps;

/** A class to collect and provide jcarbon signals. */
public final class JCarbonSystemMonitor implements JCarbon {
  private static final Logger logger = LoggerUtil.getLogger();

  private static final String OS_NAME = System.getProperty("os.name", "unknown");
  private static final String PROC_STAT = "/proc/stat";

  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-sampling-thread");
            t.setDaemon(true);
            return t;
          });
  // TODO: do we need to wire this back in?
  private final RaplSource raplSource = RaplSource.getRaplSource();
  private final EmissionsConverter converter = LocaleEmissionsConverters.forDefaultLocale();
  private final int periodMillis;

  private boolean isRunning = false;
  private SamplingFuture<MonotonicTimeSample> monotonicTimeFuture;
  private SamplingFuture<SystemSample> systemFuture;
  private SamplingFuture<Optional<?>> raplFuture;

  public JCarbonSystemMonitor(int periodMillis) {
    this.periodMillis = periodMillis;
  }

  /** Starts the sampling futures is we aren't already running. */
  @Override
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        logger.info(String.format("starting jcarbon for linux system at %d ms", periodMillis));
        monotonicTimeFuture =
            SamplingFuture.fixedPeriodMillis(MonotonicTimeSample::new, periodMillis, executor);
        systemFuture =
            SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, periodMillis, executor);
        raplFuture =
            SamplingFuture.fixedPeriodMillis(raplSource.source::get, periodMillis, executor);
        isRunning = true;
      }
    }
  }

  /**
   * Stops the sampling futures and merges the data they collected into a {@link JCarbonReport}.
   * Returns an empty {@link Optional} if jcarbon wasn't running.
   */
  // TODO: this can throw if any of the futures are empty. i don't know how to handle this yet
  @Override
  public Optional<Report> stop() {
    synchronized (this) {
      if (isRunning) {
        logger.info("stopping jcarbon");
        isRunning = false;

        Component.Builder systemComponent =
            Component.newBuilder().setComponentType("linux_system").setComponentId(OS_NAME);

        // physical signals
        logger.info("creating monotonic time signal");
        createPhysicalSignal(
                forwardApply(
                    monotonicTimeFuture.get(), JCarbonSystemMonitor::monotonicTimeDifference),
                Signal.Unit.NANOSECONDS,
                "clock_gettime(CLOCK_MONOTONIC, &ts)")
            .ifPresent(systemComponent::addSignal);

        logger.info("creating rapl energy signal");
        Optional<Signal> raplEnergy =
            createPhysicalSignal(
                forwardApply(
                    raplFuture.get().stream()
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
                forwardApply(systemFuture.get(), ProcStat::between),
                Signal.Unit.JIFFIES,
                PROC_STAT);
        systemJiffies.ifPresent(systemComponent::addSignal);
        monotonicTimeFuture = null;
        systemFuture = null;
        raplFuture = null;

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
      }
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
