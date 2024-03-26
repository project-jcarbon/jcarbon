java_plugin(
    name = "collection_test_jmh_annotation_processor",
    deps = ["@org_openjdk_jmh_jmh_generator_annprocess"],
    processor_class = "org.openjdk.jmh.generators.BenchmarkProcessor",
    visibility = ["//visibility:private"],
)

java_binary(
    name = "msr_update",
    main_class = "jcarbon.benchmarks.cpu.rapl.MsrUpdateBenchmark",
    srcs = glob(["src/main/java/jcarbon/benchmarks/cpu/rapl/*.java"]),
    deps = [
        "//src/jcarbon",
        "@org_openjdk_jmh_jmh_core",
    ],
    plugins = [":collection_test_jmh_annotation_processor"],
)

java_library(
    name = "benchmarks",
    srcs = ["src/main/java/jcarbon/benchmarks/BenchmarkUtil.java"],
    deps = ["//src/jcarbon"],
)

java_binary(
    name = "dacapo",
    main_class = "Harness",
    srcs = ["src/main/java/jcarbon/benchmarks/JCarbonCallback.java"],
    deps = [
        ":benchmarks",
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
    srcs = ["src/main/java/jcarbon/benchmarks/JCarbonPlugin.java"],
    deps = [
        ":benchmarks",
        "//src/jcarbon",
        "@renaissance//jar",
    ],
    args = [
        "--plugin !jcarbon.benchmarks.JCarbonPlugin",
    ]
)
