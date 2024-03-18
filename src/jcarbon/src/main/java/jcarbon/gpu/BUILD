java_library(
    name = "gpu",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"]),
    deps = [
        "//src/jcarbon",
        "@org_bytedeco_javacpp_presets_cuda",
    ],
)

java_binary(
    name = "smoke_test",
    main_class = "jcarbon.gpu.Nvml",
    runtime_deps = [
        ":gpu",
        "//src/jcarbon",
        "@org_bytedeco_javacpp_presets_cuda",
    ],
)
