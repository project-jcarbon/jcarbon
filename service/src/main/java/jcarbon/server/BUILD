SERVER_BINARIES=[
    "JCarbonServer.java",
]

java_library(
    name = "server_impl",
    srcs = glob(["*.java"], exclude=SERVER_BINARIES),
    deps = [
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_protos",
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_grpc",
        "//src/jcarbon-proto:jcarbon",
        "@grpc-java//stub",
    ],
    exports = [
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_protos",
        "//service/src/main/proto/jcarbon/service:jcarbon_service_java_grpc",
    ]
)

java_binary(
    name = "server",
    visibility = ["//service:__pkg__"],
    srcs = ["JCarbonServer.java"],
    main_class = "jcarbon.server.JCarbonServer",
    runtime_deps = ["@grpc-java//netty"],
    deps = [
        ":server_impl",
        "@grpc-java//api",
        "@maven//:commons_cli_commons_cli",
    ]
)
