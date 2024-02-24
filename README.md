# jRAPL

`jRAPL` is a lightweight Java API that provides access for sampling energy from [RAPL]() within a Java runtime.

## Building

To use `jRAPL` you will need an Intel Linux system.

`RAPL` is through [`msr`s](). You'll need to run `sudo modprobe msr` to enable the `msr`s.

You need a verion of `Java`, the `jni`, and `maven` to build and run the tool. You **might** be able to deal with all that using the following:

```bash
sudo apt install openjdk-11-jdk maven libjna-jni
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

## Usage

`jRAPL` provides three core structures to represent `RAPL` energy domains:

 - `EnergyReadings` that represent energy consumed on a socket by component
 - `EnergySamples` that represent energy consumed since boot.
 - `EnergyInterval` that represent energy consumed over a time range.

Users can talk with `RAPL` through either `jrapl.Rapl` or `jrapl.Powercap`, using the `sample` and `difference` methods, which will produce `EnergySamples` and `EnergyIntervals` respectively.

Example usage:

```java
EnergySample start = Rapl.sample();
fib(42);
EnergySample end = Rapl.sample();
System.out.println(Rapl.difference(start, end));
```

A simple UML describing `jRAPL`'s layout is provided [here](https://github.com/atpoverload/jRAPL/blob/main/docs/uml/jrapl-uml.pdf).

# Migration/Feature Updates

Here are the features we want to try re-integrate into this project:
 - DVFS Interactions ([pure Java implementation](https://github.com/atpoverload/thread-actuator/blob/clean-up/jdvfs/src/main/java/jdvfs/Dvfs.java))
 - Low-level energy sampling ()
 - JMH and DaCapo performance benchmarks
 - Some form of release artifact
