# jCarbon

`jCarbon` is a Java library that provides an application-level view of Intel-Linux system consumption. The view is user-actionable, granting `system awareness` to applications.

## Features

`jCarbon` provides high-granularity data sampled at sub-second periods through a simple interface. The system can provide both physical (those that come directly from a system component) and virtual signals (which are computed from other signals).

### Physical Signals
 - [`rapl energy`](https://www.intel.com/content/www/us/en/developer/articles/technical/software-security-guidance/advisory-guidance/running-average-power-limit-energy-reporting.html)
 - [`powercap energy`](https://www.kernel.org/doc/html/next/power/powercap/powercap.html)
 - [`cpu jiffies`](https://man7.org/linux/man-pages/man5/proc.5.html)
 - [`process jiffies`](https://man7.org/linux/man-pages/man5/proc.5.html)
 - [`cpufreq`](https://wiki.debian.org/CpuFrequencyScaling)

### Virtual Signals
 - `process activity`
 - `process energy`
 - `calmness`
 <!-- - `emissions` -->

## Using `jCarbon`

<!-- `jCarbon` can be used directly from its most recent release. -->
You can directly use `jcarbon.JCarbon` for access to many signals out of the box:

```java
JCarbon jcarbon = new JCarbon();
jcarbon.start();
fib(42);
JCarbonReport report = jcarbon.stop();
System.out.println(
    String.format(
        "Consumed %.6f joules",
        report.getSignal(ProcessEnergy.class).stream()
            .mapToDouble(proc -> proc.data().stream().mapToDouble(e -> e.energy).sum())
            .sum()));
```

More information about the physical signals can be found in the linked documentation. Here, we give a short overview of the virtual signals.

### `process activity`: Jiffies Attribution

Unix-based operating systems report cpu cycle usage with a unit called *jiffies*. A jiffy refers to the time between clock ticks. The jiffies for each executing cpu can be found in `/proc/stat`, while those for a process and its children tasks can be found in `/proc/<process id>/task/<task id>/stat`. Please refer to https://man7.org/linux/man-pages/man5/proc.5.html for more information regarding the jiffies system.

Since these values represent useful work, we can compute task activity equal to the ratio of the task's jiffies and the jiffies of the task's executing cpu.

$$A_{task} = J_{task} / J_{cpu} $$

This process is a little tricky due to misalignment in the jiffies updating. Updating of jiffies is done on a specific interval (usually ten milliseconds), but this is done concurrently for each reported component. So while cpus `1` and `2`, and our process's tasks `A` and `B` may all update every ten milliseconds, when this occurs is likely different. As a result, we may end up with a situation where a task's (or multiple tasks') jiffies exceeds the executing cpu's jiffies.

We need to carefully align and normalize the signals so we don't have more than 100% attributed to the process. The final attribution computation is:

$$A_{task} = J_{task} / max(1, J_{cpu}, \sum_{task}{J_{task} ,\ where \ cpu_{task} = cpu}) $$

### `process energy`: Energy Virtualization

We can extend the process activity method described above by combining it with an `energy source`, such as `rapl` or `powercap`. We could reuse the simple computation above, however, there is a corner case. Power systems do not report by logical cpu, but instead by a physical device. In the case of `rapl`, this will be a cpu socket, which contains some number of executing dice. Thus, we must do another normalization and aggregate all tasks onto the single physical device. This mapping can be produce from the details of `/proc/cpuinfo` through the `processer` and `physical id` fields. :

$$E_{task} = E_{socket} * A_{task} / max(1, \sum_{task}{A_{task} ,\ where \ cpu_{task} \in socket}) $$

### Calmness

Calmness is a comparative metric that describes similarity between the power state of two logically identical runtime. Given a program, any amount of additional work, such as profiling, will impact the runtime. In performance profiling, we typically focus on overhead or memory footprints to determine an optimal profiling state. Unfortunately, energy characterization is a little more sensitive due to local variance. Therefore, it is critical to find an optimal spot to not disturb the runtime.

To track power state, we can watch `cpufreq` as power state is typically correlated with executing frequency. We can then define a *spatial* and a *temporal* correspondence by anonymizing the cpus into frequency distributions. We choose [Freedman-Diaconis](https://en.wikipedia.org/wiki/Freedman%E2%80%93Diaconis_rule) to bucket data. The correspondence can be computed from the fraction of observation per bin.

$$ \mathcal{K} = 2 * \frac{IQR(f)}{\sqrt[3]{|cpu| * |time|}} $$

$$ f_k = \frac{k}{\mathcal{K}} * max(f) + (1 - \frac{k}{\mathcal{K}}) * min(f), where \ k \in {0..\mathcal{K}} $$

$$ \mathcal{C}_{T}(t, k) = \frac{|f'|}{|f_t|} , where \ f' \ \in f_k < f_t \ and \ f_t \leq f_k + \frac{k}{\mathcal{K}} $$

$$ \mathcal{C}_{S}(k) = \frac{\sum_t|f'|}{|f|}, where \ f' \ \in f_k \leq f_t \ and \ f_t \leq f_k + \frac{k}{\mathcal{K}} $$

Comparing two traces can be done with any distance metric. Typically pcc is a good choice since it measures covariance.

## Building from source

Building the core `jCarbon` artifact from source is done with either `bazel` or `maven`.  Although most of the components are implemented in pure Java, we still support a legacy artifact that enables low-level access to the `rapl` subsystem called [`jRAPL`](https://jrapl.github.io). While modern implementations typically prefer `powercap`, we think it is useful to keep direct access to `rapl` available when other solutions are not. However, `jRAPL` is implemented in C, so it requires the JNI to be used. Below are build steps to get `jRAPL` to work on your system if necessary. In most cases, we recommend relying on `powercap` instead.

### `jRAPL`

To use `jRAPL` you will need an Intel Linux system.

`RAPL` read through is through [`msr`s](). You'll need to run `sudo modprobe msr` to enable the `msr`s.

You need a verion of `Java`, the `jni`, and `maven` or `bazel` to build and run the tool. You **might** be able to deal with all that using the following:

```bash
sudo apt install openjdk-11-jdk bazel maven libjna-jni
```

You can confirm if `jRAPL` works on your system by running `bash smoke_test.sh`. This should output a short report detailing what `jRAPL` is able to find for your system. Example log:

```bash
jcarbon (2024-03-18 18:34:23 PM EDT) [main]: warming up...
jcarbon (2024-03-18 18:34:30 PM EDT) [main]: testing rapl...
jcarbon (2024-03-18 18:34:31 PM EDT) [main]: rapl report
 - microarchitecture: BROADWELL2
 - elapsed time: 1.000430s
 - socket: 1, package: 32.429688J, dram: 2.742676J, core: 0.000000J, gpu: 0.000000J
 - socket: 2, package: 29.896485J, dram: 1.250000J, core: 0.000000J, gpu: 0.000000J
jcarbon (2024-03-18 18:34:33 PM EDT) [main]: powercap report
 - elapsed time: 1.443513s
 - socket: 1, package: 32.310403J, dram: 2.609461J
 - socket: 2, package: 29.738266J, dram: 1.323297J
jcarbon (2024-03-18 18:34:34 PM EDT) [main]: equivalence report
 - elapsed time difference: 0.425827s
 - socket: 1, package difference: 0.021444J, dram difference: 0.048101J
 - socket: 2, package difference: 0.018143J, dram difference: 0.027690J
jcarbon (2024-03-18 18:34:34 PM EDT) [main]: all smoke tests passed!
```

A simple UML describing `jRAPL`'s layout is provided [here](https://github.com/atpoverload/jRAPL/blob/main/docs/uml/jrapl-uml.pdf).

# Migration/Feature Updates

Here are the features we want to try re-integrate into this project:
 - Emissions reporting
 - [Nvidia GPU signal](https://github.com/bytedeco/javacpp-presets/blob/master/cuda/src/gen/java/org/bytedeco/cuda/global/nvml.java#L4546)
 - DVFS Interactions ([pure Java implementation](https://github.com/atpoverload/thread-actuator/blob/clean-up/jdvfs/src/main/java/jdvfs/Dvfs.java))
 - Low-level energy sampling
 - Some form of release artifact
