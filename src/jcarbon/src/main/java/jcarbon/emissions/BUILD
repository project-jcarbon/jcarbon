java_library(
    name = "emissions",
    visibility = ["//visibility:public"],
    srcs = glob(["*.java"]),
    deps = [
        "//src/jcarbon/src/main/java/jcarbon/cpu/eflect",
        "//src/jcarbon/src/main/java/jcarbon/cpu/rapl",
        "//src/jcarbon/src/main/java/jcarbon/data",
        "//src/jcarbon/src/main/java/jcarbon/util",
    ],
    resources = ["//src/jcarbon/src/main/resources/emissions:emissions_data"],
)
