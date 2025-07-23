load("@protobuf//bazel:java_proto_library.bzl", "java_proto_library")
load("@grpc-java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "jcarbon_service_protos",
    srcs = ["jcarbon_service.proto"],
    deps = ["//src/jcarbon-proto/src/main/proto/jcarbon/signal:signal_protos"]
)

java_proto_library(
    name = "jcarbon_service_java_protos",
    visibility = ["//visibility:public"],
    deps = [":jcarbon_service_protos"]
)

java_grpc_library(
    name = "jcarbon_service_java_grpc",
    visibility = ["//visibility:public"],
    srcs = [":jcarbon_service_protos"],
    deps = [":jcarbon_service_java_protos"]
)
