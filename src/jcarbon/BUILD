java_library(
    name = "jcarbon",
    visibility = ["//visibility:public"],
    srcs = glob([
        "src/main/java/jcarbon/cpu/**/*.java",
        "src/main/java/jcarbon/data/*.java",
        "src/main/java/jcarbon/util/*.java",
        "src/main/java/jcarbon/emissions/*.java",
        "src/main/java/jcarbon/*.java",
    ]),
    resources = [
        ":emissions_data",
        "//src/native:libjrapl.so",
    ],
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

filegroup(
    name = "emissions_data",
    srcs = ["src/main/resources/emissions/WorldIntensity.csv"],
)
