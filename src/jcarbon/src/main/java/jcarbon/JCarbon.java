package jcarbon;

import static java.util.stream.Collectors.toList;
import static jcarbon.data.DataOperations.forwardApply;
import static jcarbon.data.DataOperations.forwardPartialAlign;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import jcarbon.cpu.eflect.EflectAccounting;
import jcarbon.cpu.eflect.ProcessEnergy;
import jcarbon.cpu.jiffies.JiffiesAccounting;
import jcarbon.cpu.jiffies.ProcStat;
import jcarbon.cpu.jiffies.ProcTask;
import jcarbon.cpu.jiffies.ProcessActivity;
import jcarbon.cpu.jiffies.ProcessJiffies;
import jcarbon.cpu.jiffies.ProcessSample;
import jcarbon.cpu.jiffies.SystemJiffies;
import jcarbon.cpu.jiffies.SystemSample;
import jcarbon.cpu.rapl.RaplEnergy;
import jcarbon.cpu.rapl.RaplSample;
import jcarbon.cpu.rapl.RaplSource;
import jcarbon.emissions.Emissions;
import jcarbon.emissions.EmissionsConverter;
import jcarbon.emissions.LocaleEmissionsConverters;
import jcarbon.util.LoggerUtil;
import jcarbon.util.SamplingFuture;

/** A class to collect and provide jcarbon signals. */
public final class JCarbon {
  private static final Logger logger = LoggerUtil.getLogger();

  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "jcarbon-sampling-thread");
            t.setDaemon(true);
            return t;
          });
  private final RaplSource source = RaplSource.getRaplSource();
  private final EmissionsConverter converter = LocaleEmissionsConverters.forDefaultLocale();
  private final int periodMillis;
  private final OptionalLong processId;

  private boolean isRunning = false;
  private SamplingFuture<ProcessSample> processFuture;
  private SamplingFuture<SystemSample> systemFuture;
  private SamplingFuture<Optional<RaplSample>> raplFuture;

  public JCarbon(int periodMillis) {
    this.periodMillis = periodMillis;
    this.processId = OptionalLong.empty();
  }

  public JCarbon(int periodMillis, long processId) {
    this.periodMillis = periodMillis;
    this.processId = OptionalLong.of(processId);
  }

  /** Starts the sampling futures is we aren't already running. */
  public void start() {
    synchronized (this) {
      if (!isRunning) {
        logger.info(
            String.format(
                "starting jcarbon for process %d at %d ms",
                processId.orElseGet(() -> ProcessHandle.current().pid()), periodMillis));
        if (processId.isEmpty()) {
          processFuture =
              SamplingFuture.fixedPeriodMillis(ProcTask::sampleTasks, periodMillis, executor);
        } else {
          processFuture =
              SamplingFuture.fixedPeriodMillis(
                  () -> ProcTask.sampleTasksFor(processId.getAsLong()), periodMillis, executor);
        }
        systemFuture =
            SamplingFuture.fixedPeriodMillis(ProcStat::sampleCpus, periodMillis, executor);
        raplFuture = SamplingFuture.fixedPeriodMillis(source::sample, periodMillis, executor);
        isRunning = true;
      }
    }
  }

  /**
   * Stops the sampling futures and merges the data they collected into a {@link JCarbonReport}.
   * Returns an empty {@link Optional} if jcarbon wasn't running.
   */
  // TODO: this can throw if any of the futures are empty. i don't know how to handle this yet
  public Optional<JCarbonReport> stop() {
    synchronized (this) {
      if (isRunning) {
        logger.info("stopping jcarbon");
        isRunning = false;

        JCarbonReport report = new JCarbonReport();

        // physical signals
        report.addSignal(
            ProcessJiffies.class, forwardApply(processFuture.get(), ProcessJiffies::between));
        report.addSignal(
            SystemJiffies.class, forwardApply(systemFuture.get(), SystemJiffies::between));
        List<RaplSample> raplSamples =
            raplFuture.get().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        if (raplSamples.size() > 1) {
          report.addSignal(RaplEnergy.class, forwardApply(raplSamples, source::difference));
        } else {
          logger.info("no samples found for rapl");
        }
        processFuture = null;
        systemFuture = null;
        raplFuture = null;

        // virtual signals
        List<ProcessActivity> activity =
            forwardPartialAlign(
                report.getSignal(ProcessJiffies.class),
                report.getSignal(SystemJiffies.class),
                JiffiesAccounting::computeTaskActivity);
        if (activity.size() > 1) {
          report.addSignal(
              ProcessActivity.class,
              forwardPartialAlign(
                  report.getSignal(ProcessJiffies.class),
                  report.getSignal(SystemJiffies.class),
                  JiffiesAccounting::computeTaskActivity));
        } else {
          logger.info("no activity could be produced");
        }
        if (report.hasSignal(RaplEnergy.class) && report.hasSignal(ProcessActivity.class)) {
          report.addSignal(
              ProcessEnergy.class,
              forwardPartialAlign(
                  report.getSignal(ProcessActivity.class),
                  report.getSignal(RaplEnergy.class),
                  EflectAccounting::computeTaskEnergy));
        } else {
          logger.info("no process energy could be produced");
        }
        if (report.hasSignal(ProcessEnergy.class)) {
          report.addSignal(
              Emissions.class,
              report.getSignal(ProcessEnergy.class).stream()
                  .map(nrg -> converter.convert(nrg))
                  .collect(toList()));
        } else {
          logger.info("no emissions could be produced");
        }
        return Optional.of(report);
      }
    }
    return Optional.empty();
  }
}
