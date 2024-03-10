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

java_binary(
    name = "dacapo",
    main_class = "Harness",
    srcs = glob(["src/main/java/jcarbon/dacapo/*.java"]),
    deps = [
        "//src/jcarbon",
        "@dacapo",
    ],
    args = [
        "--iterations 20",
        "--callback jcarbon.dacapo.JCarbonCallback",
        "--no-validation",
    ]
)

java_binary(
    name = "renaissance",
    main_class = "org.renaissance.core.Launcher",
    srcs = glob(["src/main/java/jcarbon/renaissance/*.java"]),
    deps = [
        "//src/jcarbon",
        "@renaissance//jar",
    ],
    args = [
        "--repetitions 20",
        "--plugin !jcarbon.renaissance.JCarbonPlugin",
    ]
)
