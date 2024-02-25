java_library(
    name = "jcarbon",
    visibility = ["//visibility:public"],
    srcs = glob([
        "src/main/java/jcarbon/cpu/**/*.java",
        "src/main/java/jcarbon/data/*.java",
        "src/main/java/jcarbon/util/*.java",
    ])
)

java_binary(
    name = "smoke_test",
    main_class = "jcarbon.cpu.rapl.SmokeTest",
    runtime_deps = [":jcarbon"],
)
