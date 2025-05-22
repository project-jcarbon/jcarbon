BINARY_SRCS = ["PowercapSmokeTest.java"]

java_library(
    name = "powercap",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"], exclude = BINARY_SRCS),
    deps = [
        "//src/jcarbon-proto/src/main/java/jcarbon/util",
        "//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos",
    ],
)

java_binary(
    name = "smoke_test",
    main_class = "jcarbon.linux.powercap.PowercapSmokeTest",
    srcs = ["PowercapSmokeTest.java"],
    deps = [
        ":powercap",
        "//src/jcarbon-proto/src/main/java/jcarbon/util",
        "//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos",
    ],
)