load("@rules_proto_grpc//java:defs.bzl", "java_proto_library")

proto_library(
    name = "signal_protos",
    visibility = ["//visibility:public"],
    srcs = ["signal.proto"],
)

java_proto_library(
    name = "signal_java_protos",
    visibility = ["//visibility:public"],
    protos = [":signal_protos"],
)
