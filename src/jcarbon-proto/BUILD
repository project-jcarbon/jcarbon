alias(
    name = "jcarbon",
    visibility = ["//visibility:public"],
    actual = "//src/jcarbon-proto/src/main/java/jcarbon",
)

java_binary(
    name = "rapl_smoke_test",
    main_class = "jcarbon.cpu.rapl.RaplSmokeTest",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "rapl_monitor",
    main_class = "jcarbon.cpu.rapl.RaplMonitor",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "cpufreq_monitor",
    main_class = "jcarbon.cpu.freq.CpuFreqMonitor",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "cputemp_monitor",
    main_class = "jcarbon.linux.temp.TemperatureMonitor",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "cpu_temp",
    main_class = "jcarbon.linux.temp.Temperature",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "test",
    main_class = "jcarbon.linux.jiffies.SystemSample",
    runtime_deps = [":jcarbon"],
)

alias(
    name = "emissions_data",
    actual = "//src/main/resources/emissions/WorldIntensity.csv",
)
