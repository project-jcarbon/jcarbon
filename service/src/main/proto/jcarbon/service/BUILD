load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("@rules_proto_grpc//python:defs.bzl", "python_grpc_library")

proto_library(
    name = "jcarbon_service_protos",
    srcs = ["jcarbon_service.proto"],
    deps = ["//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_protos"]
)

java_grpc_library(
    name = "jcarbon_service_java_grpc",
    visibility = ["//visibility:public"],
    protos = [":jcarbon_service_protos"],
    deps = ["//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_java_protos"]
)
