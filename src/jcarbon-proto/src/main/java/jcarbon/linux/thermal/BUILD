load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "thermal",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"]),
    deps = [
        "//src/jcarbon-proto/src/main/java/jcarbon/util",
        "//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos",
        artifact("commons-cli:commons-cli"),
    ],
)
