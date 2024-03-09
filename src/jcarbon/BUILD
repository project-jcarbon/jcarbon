java_library(
    name = "jcarbon",
    visibility = ["//visibility:public"],
    srcs = glob([
        "src/main/java/jcarbon/cpu/**/*.java",
        "src/main/java/jcarbon/data/*.java",
        "src/main/java/jcarbon/util/*.java",
        "src/main/java/jcarbon/*.java",
    ])
)

java_binary(
    name = "smoke_test",
    main_class = "jcarbon.cpu.rapl.SmokeTest",
    runtime_deps = [":jcarbon"],
)

java_binary(
    name = "dvfs_monitor",
    main_class = "jcarbon.cpu.freq.DvfsMonitor",
    runtime_deps = [":jcarbon"],
)
