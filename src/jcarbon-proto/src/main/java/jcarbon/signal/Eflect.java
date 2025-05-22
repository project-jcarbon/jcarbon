package jcarbon.signal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import sigprop.signal.SubscribeableSignal;

public final class Eflect extends SubscribeableSignal<Map<Integer, TaskPower>>
    implements SinkSignal<TaskPower> {
  private static final int[] SOCKET_MAPPING;

  private final SourceSignal<TaskActivity> activity;
  private final SourceSignal<Power> power;

  public Eflect(SourceSignal<Map<Integer, TaskActivity>> activity, SourceSignal<double[]> power) {
    this.activity = activity;
    this.power = power;
  }

  @Override
  public Map<Integer, TaskPower> sample(Instant timestamp) {
    Map<Integer, TaskActivity> activity = this.activity.sample(timestamp);
    double[] power = this.power.sample(timestamp);
    HashMap<Integer, TaskPower> energy = new HashMap<>();
    activity.forEach(
        (id, task) ->
            energy.put(id, new TaskPower(task, task.power * power[SOCKET_MAPPING[task.cpu]])));
  }

  public static void main(String[] args) throws Exception {
    Eflect eflect =
        SampledSource.fixedPeriodMillis(ProcTask::new, 10, executor)
            .map(SymbolicSignal::new)
            .compose(
                SampledSource.fixedPeriodMillis(ProcStat::new, 10, executor)
                    .map(SymbolicSignal::new),
                TaskActivity::new)
            .compose(
                SampledSource.fixedPeriodMillis(Powercap::new, 10, executor)
                    .map(SymbolicSignal::new),
                Eflect::new)
            .map(OnlineProfiler::new);
    Thread.sleep(1000);
    executor.shutdown();
  }
}
