BINARY_SRCS = ["RaplSmokeTest.java"]

java_library(
    name = "rapl",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"], exclude = BINARY_SRCS),
    deps = [
        "//src/jcarbon-proto/src/main/java/jcarbon/util",
        "//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos",
    ],
    resources = ["//src/jcarbon-proto/src/main/c/jcarbon/rapl:libjrapl.so"],
)

java_binary(
    name = "smoke_test",
    main_class = "jcarbon.rapl.RaplSmokeTest",
    srcs = ["RaplSmokeTest.java"],
    deps = [
        ":rapl",
        "//src/jcarbon-proto/src/main/java/jcarbon/util",
        "//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos",
    ],
)