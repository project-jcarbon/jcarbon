package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.util.DataOperations.forwardApply;
import static jcarbon.util.DataOperations.forwardPartialAlign;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.LocaleEmissionsConverters;
import jcarbon.linux.jiffies.EflectAccounting;
import jcarbon.linux.jiffies.JiffiesAccounting;
import jcarbon.linux.jiffies.ProcStat;
import jcarbon.linux.jiffies.ProcTask;
import jcarbon.linux.jiffies.ProcessSample;
import jcarbon.linux.jiffies.SystemSample;
import jcarbon.linux.powercap.Powercap;
import jcarbon.linux.powercap.PowercapSample;
import jcarbon.signal.Component;
import jcarbon.signal.Report;
import jcarbon.signal.Signal;
import jcarbon.signal.SignalInterval;
import jcarbon.util.LoggerUtil;
import jcarbon.util.SamplingFuture;

/** A class to collect and provide jcarbon signals. */
public final class JCarbon {
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
  // private final RaplSource raplSource = RaplSource.getRaplSource();
  private final EmissionsConverter converter = LocaleEmissionsConverters.forDefaultLocale();
  private final int periodMillis;
  private final long processId;

  private boolean isRunning = false;
  private SamplingFuture<ProcessSample> processFuture;
  private SamplingFuture<SystemSample> systemFuture;
  private SamplingFuture<Optional<PowercapSample>> raplFuture;

  public JCarbon(int periodMillis) {
    this.periodMillis = periodMillis;
    this.processId = ProcessHandle.current().pid();
  }

  public JCarbon(int periodMillis, long processId) {
    this.periodMillis = periodMillis;
    this.processId = processId;
  }

  /** Starts the sampling futures is we aren't already running. */
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        logger.info(
            String.format("starting jcarbon for process %d at %d ms", processId, periodMillis));
        processFuture =
            SamplingFuture.fixedPeriodMillis(
                () -> ProcTask.sampleTasksFor(processId), periodMillis, executor);
        systemFuture =
            SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, periodMillis, executor);
        raplFuture = SamplingFuture.fixedPeriodMillis(Powercap::sample, periodMillis, executor);
        isRunning = true;
      }
    }
  }

  /**
   * Stops the sampling futures and merges the data they collected into a {@link JCarbonReport}.
   * Returns an empty {@link Optional} if jcarbon wasn't running.
   */
  // TODO: this can throw if any of the futures are empty. i don't know how to handle this yet
  public Optional<Report> stop() {
    synchronized (this) {
      if (isRunning) {
        logger.info("stopping jcarbon");
        isRunning = false;

        String procTask = String.format("/proc/%d/task", processId);
        Component.Builder processComponent =
            Component.newBuilder()
                .setComponentType("linux_process")
                .setComponentId(Long.toString(processId));
        Component.Builder systemComponent =
            Component.newBuilder().setComponentType("linux_system").setComponentId(OS_NAME);

        // physical signals
        logger.info("creating rapl energy signal");
        Optional<Signal> raplEnergy =
            createPhysicalSignal(
                forwardApply(
                    raplFuture.get().stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toList()),
                    Powercap::difference),
                Signal.Unit.JOULES,
                "/sys/devices/virtual/powercap/intel-rapl");
        systemJiffies.ifPresent(systemComponent::addSignal);
        logger.info("creating process jiffies signal");
        Optional<Signal> processJiffies =
            createPhysicalSignal(
                forwardApply(processFuture.get(), ProcTask::between),
                Signal.Unit.JIFFIES,
                procTask);
        processJiffies.ifPresent(processComponent::addSignal);
        logger.info("creating system jiffies signal");
        Optional<Signal> systemJiffies =
            createPhysicalSignal(
                forwardApply(systemFuture.get(), ProcStat::between),
                Signal.Unit.JIFFIES,
                PROC_STAT);
        systemJiffies.ifPresent(systemComponent::addSignal);
        processFuture = null;
        systemFuture = null;
        raplFuture = null;

        // virtual signals
        if (raplEnergy.isEmpty()) {
          logger.info("not creating rapl emissions: no rapl energy");
        } else {
          logger.info("creating rapl emissions signal");
          systemComponent.addSignal(converter.convert(raplEnergy.get()));
        }

        if (processJiffies.isEmpty() && systemJiffies.isEmpty()) {
          logger.info("not creating linux process activity: no jiffies");
        } else {
          List<SignalInterval> activity =
              forwardPartialAlign(
                  processJiffies.get().getIntervalList(),
                  systemJiffies.get().getIntervalList(),
                  JiffiesAccounting::computeTaskActivity);
          if (activity.size() > 0) {
            logger.info("creating linux process activity signal");
            processComponent.addSignal(
                Signal.newBuilder()
                    .setUnit(Signal.Unit.ACTIVITY)
                    .addSource(procTask)
                    .addSource(PROC_STAT)
                    .addAllInterval(activity));
            if (raplEnergy.isPresent()) {
              logger.info("creating linux process energy signal");
              Signal processEnergy =
                  Signal.newBuilder()
                      .setUnit(Signal.Unit.JOULES)
                      .addSource(procTask)
                      .addSource(PROC_STAT)
                      .addSource("/sys/devices/virtual/powercap/intel-rapl")
                      .addAllInterval(
                          forwardPartialAlign(
                              activity,
                              raplEnergy.get().getIntervalList(),
                              EflectAccounting::computeTaskEnergy))
                      .build();
              processComponent.addSignal(processEnergy);

              logger.info("creating linux process emissions signal");
              processComponent.addSignal(converter.convert(processEnergy));
            } else {
              logger.info("not creating linux process energy: no rapl energy");
            }
          } else {
            logger.info("not creating linux process activity: no activity");
          }
        }
        Report.Builder report = Report.newBuilder();
        if (systemComponent.getSignalCount() > 0) {
          report.addComponent(systemComponent);
        }
        if (processComponent.getSignalCount() > 0) {
          report.addComponent(processComponent);
        }
        if (report.getComponentCount() > 0) {
          return Optional.of(report.build());
        }
      }
    }
    return Optional.empty();
  }

  private Optional<Signal> getPhysicalSignal(
      List<SignalInterval> intervals, Signal.Unit unit, String source) {
    if (intervals.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        Signal.newBuilder().setUnit(unit).addSource(source).addAllInterval(intervals).build());
  }
}
