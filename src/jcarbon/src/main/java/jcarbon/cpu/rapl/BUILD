BINARY_SRCS = [
    "jcarbon.cpu.rapl.RaplMonitor.java",
    "jcarbon.cpu.rapl.RaplSmokeTest.java",
]

java_library(
    name = "rapl",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"], exclude = BINARY_SRCS),
    deps = [
        "//src/jcarbon/src/main/java/jcarbon/cpu",
        "//src/jcarbon/src/main/java/jcarbon/data",
        "//src/jcarbon/src/main/java/jcarbon/util",
    ],
    resources = ["//src/native:libjrapl.so"],
)

java_binary(
    name = "rapl_smoke_test",
    main_class = "jcarbon.cpu.rapl.RaplSmokeTest",
    srcs = ["RaplSmokeTest.java"],
    deps = [
        ":rapl",
        "//src/jcarbon/src/main/java/jcarbon/util",
    ],
)

java_binary(
    name = "rapl_monitor",
    main_class = "jcarbon.cpu.rapl.RaplMonitor",
    srcs = ["RaplMonitor.java"],
    deps = [
        ":rapl",
        "//src/jcarbon/src/main/java/jcarbon/util",
    ],
)
