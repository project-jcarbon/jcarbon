SERVER_BINARIES=[
    "JCarbonServer.java",
]

java_library(
    name = "server_impl",
    srcs = glob(["*.java"], exclude=SERVER_BINARIES),
    deps = [
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_grpc",
        "//src/jcarbon",
        "//src/jcarbon/src/main/java/jcarbon/data",
        "@maven//:org_json_json",
    ]
)


java_binary(
    name = "server",
    srcs = ["JCarbonServer.java"],
    main_class = "jcarbon.server.JCarbonServer",
    runtime_deps = ["@io_grpc_grpc_java//netty"],
    deps = [
        ":server_impl",
        "@io_grpc_grpc_java//api",
        # "@io_grpc_grpc_java//protobuf",
        # "@io_grpc_grpc_java//stub",
        # "@maven//:com_google_api_grpc_proto_google_common_protos",
        # "@maven//:com_google_code_findbugs_jsr305",
        # "@maven//:com_google_code_gson_gson",
        # "@maven//:com_google_guava_guava",
    ]
)
