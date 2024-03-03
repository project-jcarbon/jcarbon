# jCarbon

`jCarbon` is a Java library that provides an application-level view of Intel-Linux system consumption. The view is user-actionable, granting `system awareness` to applications.

## Features

`jCarbon` provides both physical and virtual signals. 

### Physical Signals
 - `rapl energy`
 - `powercap energy`
 - `cpu jiffies`
 - `task jiffies`
 - `cpufreq`

### Virtual Signals
 - `task activity`
 - `task energy`
 <!-- - `emissions` -->
 - `calmness`

## Using `jCarbon`

<!-- `jCarbon` can be used directly from its most recent release. You can directly use `jcarbon.JCarbon` for a complete out-of-the-box report:


```java
JCarbon jcarbon = new JCarbon();
jcarbon.start();
fib(42);
List<Footprint> footprints = jcarbon.stop();
footprints.forEach(System.out::println);
```

Under the hood, `JCarbon` will determine the correct signals and metadata to produce an application-specific consumption report. -->

More information about the physical signals can be found in the linked documentation. Here, we give a short overview of the virtual signals.

### Jiffies Attribution

Unix-based operating systems report cpu cycle usage with a unit called *jiffies*. A jiffy refers to the time between clock ticks. The jiffies for each executing cpu can be found in `/proc/stat`, while those for a process and its children tasks can be found in `/proc/<process id>/task/<task id>/stat`. Please refer to https://man7.org/linux/man-pages/man5/proc.5.html for more information regarding the jiffies system.

Since these values represent useful work, we can compute `task activity` equal to the ratio of the task's jiffies and the jiffies of the task's executing cpu.

$$A_{task} = J_{task} / J_{cpu} $$

This process is a little tricky due to misalignment in the jiffies updating. Updating of jiffies is done on a specific interval (usually ten milliseconds), but this is done concurrently for each reported component. So while cpus `1` and `2`, and process `A`'s children tasks `B` and `C` may all update every ten milliseconds, when this occurs is likely different. As a result, we may end up with a situation where a task's (or multiple tasks') jiffies exceeds the executing cpu's jiffies. Thus, we need to carefully align and normalize the signals so we don't have more than 100% attributed to the process. Thus, the final attribution computation is:

$$A_{task} = J_{task} / max(1, J_{cpu}, \sum_{task}{J_{task} ,\ where \ task.cpu=cpu}) $$

### Energy Virtualization

We can extend the `task activity` method described above by combining it with an `energy source`, such as `rapl` or `powercap`. We could reuse the simple computation above, however, there is a corner case. Power systems do not report by logical cpu, but instead by a physical device. In the case of `rapl`, this will be a cpu socket, which contains some number of executing dice. Thus, we must do another normalization and aggregate all tasks onto the single physical device. This mapping can be produce from the details of `/proc/cpuinfo` through the `processer` and `physical id` fields. :

$$E_{task} = E_{socket} * A_{task} / max(1, \sum_{task}{A_{task} ,\ where \ task.cpu \in socket}) $$

### Calmness

Calmness is a comparative metric that describes similarity between the power state of two logically identical runtime. While any runtime will be impacted by profiling, energy characterization is a little more sensitive since power state will vary during program execution. Therefore, it is critical to find an optimal spot to not disturb the runtime. In performance profiling, we typically focus exclusively on overhead or memory footprint to determine an optimal profiling state. This is not so with energy, which can have local variations during profiling.

To track power state, we can watch `cpufreq` as power state is typically correlated with executing frequency. We can then define a *spatial* and a *temporal* correspondence by anonymizing the cpus into frequency distributions. We choose [Freedman-Diaconis](https://en.wikipedia.org/wiki/Freedman%E2%80%93Diaconis_rule) to bucket data. The correspondence can be computed from the fraction of observation per bin.

$$ \mathcal{K} = 2 * \frac{IQR(f)}{\sqrt[3]{|cpu| * |time|}} $$

$$ f_{k} = \frac{k}{\mathcal{K}} * max(f) + (1 - \frac{k}{\mathcal{K}}) * min(f), where \ k \in {0..\mathcal{K}} $$

$$ \mathcal{C}_{T}(t, k) = \frac{|f'|}{|f_t|} $$
 
 <!-- , where \ f' \ \in f_{k} \leq f_{t} \ and \ f_{t} \leq f_{k} + \frac{k}{\mathcal{K}} $$ -->

$$\mathcal{C}_{S}(k) = \frac{\sum_{t}|f'|}{|f|}, where \ f' \ \in f_{k} \leq f_{t} \ and \ f_{t} \leq f_{k} + \frac{k}{\mathcal{K}} $$

Comparing two traces can be done with any distance metric. Typically pcc is a good choice since it measures covariance.

## Building from source

Building the core `jCarbon` artifact from source is done with either `bazel` or `maven`.  Although most of the components are implemented in pure Java, we still support a legacy artifact that enables low-level access to the `rapl` subsystem called [`jRAPL`](). While modern implementations typically prefer `powercap`, we think it is useful to keep direct access to `rapl` available when other solutions are not. However, `jRAPL` is implemented in C, so it requires the JNI to be used. Below are build steps to get `jRAPL` to work on your system if necessary. In most cases, we recommend relying on `powercap` instead.

### `jRAPL`

To use `jRAPL` you will need an Intel Linux system.

`RAPL` is through [`msr`s](). You'll need to run `sudo modprobe msr` to enable the `msr`s.

You need a verion of `Java`, the `jni`, and `maven` or `bazel` to build and run the tool. You **might** be able to deal with all that using the following:

```bash
sudo apt install openjdk-11-jdk bazel, maven, libjna-jni
```

You can confirm if `jRAPL` works on your system by running `bash smoke_test.sh`. This should output a short report detailing what `jRAPL` is able to find for your system. Example log:

```bash
jrapl (2024-02-19 11:14:26 AM EST) [main]: warming up...
jrapl (2024-02-19 11:14:34 AM EST) [main]: testing rapl...
jrapl (2024-02-19 11:14:35 AM EST) [main]: rapl report
 - microarchitecture: BROADWELL2
 - elapsed time: 1.000509s
 - socket: 1, package: 35.547J, dram: 1.389J, core: 0.000J, gpu: 0.000J
jrapl (2024-02-19 11:14:37 AM EST) [main]: powercap report
 - elapsed time: 1.519122s
 - socket: 1, package: 36.170J, dram: 1.451J
jrapl (2024-02-19 11:14:38 AM EST) [main]: equivalence report
 - elapsed time difference: 0.514607s
 - socket: 1, package difference: 0.000J, dram difference: 0.026J
jrapl (2024-02-19 11:14:38 AM EST) [main]: all smoke tests passed!
```

A simple UML describing `jRAPL`'s layout is provided [here](https://github.com/atpoverload/jRAPL/blob/main/docs/uml/jrapl-uml.pdf).

# Migration/Feature Updates

Here are the features we want to try re-integrate into this project:
 - Emissions reporting
 - DVFS Interactions ([pure Java implementation](https://github.com/atpoverload/thread-actuator/blob/clean-up/jdvfs/src/main/java/jdvfs/Dvfs.java))
 - Low-level energy sampling
 - Some form of release artifact
