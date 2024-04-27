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
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_grpc",
        "@io_grpc_grpc_java//api",
        "@maven//:commons_cli_commons_cli",
    ]
)
