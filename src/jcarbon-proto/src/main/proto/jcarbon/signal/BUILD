load("@protobuf//bazel:java_proto_library.bzl", "java_proto_library")

proto_library(
    name = "signal_protos",
    visibility = ["//visibility:public"],
    srcs = ["signal.proto"],
)

java_proto_library(
    name = "signal_java_protos",
    visibility = ["//visibility:public"],
    deps = [":signal_protos"],
)
