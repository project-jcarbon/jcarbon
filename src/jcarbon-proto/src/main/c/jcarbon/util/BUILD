cc_library(
    name = "time-lib",
    visibility = ["//src/jcarbon-proto/src/main/c/jcarbon/rapl:__pkg__"],
    srcs = glob(["*.c"]),
    hdrs = glob(["*.h"]),
    deps = ["//src/jcarbon-proto/src/main/c:copy_jni_hdr_lib"],
    alwayslink = True,
)

cc_binary(
    name = "libtime.so",
    visibility = ["//src/jcarbon-proto/src/main/java/jcarbon/util:__pkg__"],
    linkshared = True,
    data = glob(["*.h"]),
    deps = [":time-lib"],
)
