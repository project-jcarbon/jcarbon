BENCHMARK_SRCS = [
    "JCarbonCallback.java",
    "JCarbonPlugin.java",
]

java_binary(
    name = "dacapo",
    main_class = "Harness",
    visibility = ["//benchmarks:__pkg__"],
    srcs = ["JCarbonCallback.java"],
    deps = [
        "//benchmarks/src/main/java/jcarbon/benchmarks/util",
        "//src/jcarbon",
        "@dacapo",
    ],
    args = [
        "--callback jcarbon.benchmarks.JCarbonCallback",
        "--no-validation",
    ]
)

java_binary(
    name = "renaissance",
    main_class = "org.renaissance.core.Launcher",
    visibility = ["//benchmarks:__pkg__"],
    srcs = ["JCarbonPlugin.java"],
    deps = [
        "//benchmarks/src/main/java/jcarbon/benchmarks/util",
        "//src/jcarbon/src/main/java/jcarbon",
        "@renaissance//jar",
    ],
    args = [
        "--plugin !jcarbon.benchmarks.JCarbonPlugin",
    ]
)
