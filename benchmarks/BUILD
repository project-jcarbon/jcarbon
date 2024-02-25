java_library(
    name = "benchmarks",
    srcs = glob([
        "src/main/java/jcarbon/dacapo/*.java",
    ]),
    deps = [
        "//src/jcarbon",
        "@dacapo",
    ],
)

java_binary(
    name = "dacapo",
    main_class = "Harness",
    runtime_deps = [
        ":benchmarks",
    ],
    args = [
        "--iterations 20",
        "--callback jcarbon.dacapo.JCarbonCallback",
        "--no-validation",
    ]
)
