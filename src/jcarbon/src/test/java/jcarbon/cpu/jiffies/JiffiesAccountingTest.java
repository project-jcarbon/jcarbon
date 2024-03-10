package jcarbon.cpu.jiffies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JiffiesAccountingTest {
  private static final int CPU = 0;
  private static final int PID = 1;
  private static final int TID_1 = 2;
  private static final int TID_2 = 3;

  private static final Instant ZERO = Instant.EPOCH;
  private static final Instant ONE = Instant.EPOCH.plusMillis(1);
  private static final Instant TWO = Instant.EPOCH.plusMillis(2);

  @Test
  public void computeTaskActivity1() {
    ProcessJiffies process =
        new ProcessJiffies(ZERO, ONE, PID, List.of(createTaskJiffies(TID_1, 1)));
    SystemJiffies system = new SystemJiffies(ZERO, ONE, new CpuJiffies[] {createCpuJiffies(2)});

    ProcessActivity activity = JiffiesAccounting.computeTaskActivity(process, system).get();

    assertEquals(ZERO, activity.start());
    assertEquals(ONE, activity.end());
    assertEquals(1, activity.data().size());
    assertEquals(TID_1, activity.data().get(0).taskId);
    assertEquals(0.50, activity.data().get(0).activity, 0.0);
  }

  @Test
  public void computeTaskActivity2() {
    ProcessJiffies process =
        new ProcessJiffies(
            ZERO, ONE, PID, List.of(createTaskJiffies(TID_1, 1), createTaskJiffies(TID_2, 2)));
    SystemJiffies system = new SystemJiffies(ZERO, ONE, new CpuJiffies[] {createCpuJiffies(4)});

    ProcessActivity activity = JiffiesAccounting.computeTaskActivity(process, system).get();

    assertEquals(ZERO, activity.start());
    assertEquals(ONE, activity.end());
    assertEquals(2, activity.data().size());
    assertEquals(TID_1, activity.data().get(0).taskId);
    assertEquals(0.25, activity.data().get(0).activity, 0.0);
    assertEquals(TID_2, activity.data().get(1).taskId);
    assertEquals(0.50, activity.data().get(1).activity, 0.0);
  }

  @Test
  public void computeTaskActivity_notEqualTimeRanges() {
    ProcessJiffies process =
        new ProcessJiffies(ZERO, ONE, PID, List.of(createTaskJiffies(TID_1, 1)));
    SystemJiffies system = new SystemJiffies(ZERO, TWO, new CpuJiffies[] {createCpuJiffies(2)});

    ProcessActivity activity = JiffiesAccounting.computeTaskActivity(process, system).get();

    assertEquals(ZERO, activity.start());
    assertEquals(ONE, activity.end());
    assertEquals(1, activity.data().size());
    assertEquals(0.50, activity.data().get(0).activity, 0.0);
  }

  @Test
  public void computeTaskActivity_tooManyTaskJiffies1() {
    ProcessJiffies process =
        new ProcessJiffies(ZERO, ONE, PID, List.of(createTaskJiffies(TID_1, 3)));
    SystemJiffies system = new SystemJiffies(ZERO, TWO, new CpuJiffies[] {createCpuJiffies(2)});

    ProcessActivity activity = JiffiesAccounting.computeTaskActivity(process, system).get();

    assertEquals(ZERO, activity.start());
    assertEquals(ONE, activity.end());
    assertEquals(1, activity.data().size());
    assertEquals(1.0, activity.data().get(0).activity, 0.0);
  }

  @Test
  public void computeTaskActivity_tooManyTaskJiffies2() {
    ProcessJiffies process =
        new ProcessJiffies(
            ZERO, ONE, PID, List.of(createTaskJiffies(TID_1, 1), createTaskJiffies(TID_2, 3)));
    SystemJiffies system = new SystemJiffies(ZERO, TWO, new CpuJiffies[] {createCpuJiffies(2)});

    ProcessActivity activity = JiffiesAccounting.computeTaskActivity(process, system).get();

    assertEquals(ZERO, activity.start());
    assertEquals(ONE, activity.end());
    assertEquals(2, activity.data().size());
    assertEquals(TID_1, activity.data().get(0).taskId);
    assertEquals(0.25, activity.data().get(0).activity, 0.0);
    assertEquals(TID_2, activity.data().get(1).taskId);
    assertEquals(0.75, activity.data().get(1).activity, 0.0);
  }

  @Test
  public void computeTaskActivity_noOverlap() {
    ProcessJiffies process =
        new ProcessJiffies(ZERO, ONE, PID, List.of(new TaskJiffies(TID_1, PID, CPU, 1, 1)));
    SystemJiffies system =
        new SystemJiffies(TWO, TWO.plusMillis(1), new CpuJiffies[] {createCpuJiffies(2)});

    assertTrue(JiffiesAccounting.computeTaskActivity(process, system).isEmpty());
  }

  private static TaskJiffies createTaskJiffies(int taskId, int jiffies) {
    return new TaskJiffies(taskId, PID, CPU, jiffies, 0);
  }

  private static CpuJiffies createCpuJiffies(int jiffies) {
    return new CpuJiffies(CPU, jiffies, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  }
}
