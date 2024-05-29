cc_library(
    name = "jrapl-lib",
    srcs = glob(["*.c"]),
    hdrs = glob(["*.h"]),
    deps = [
      "//src/jcarbon-proto/src/main/c:copy_jni_hdr_lib",
      "//src/jcarbon-proto/src/main/c/jcarbon/util:time-lib",
    ],
    alwayslink = True,
)

cc_binary(
    name = "libjrapl.so",
    visibility = ["//src/jcarbon-proto/src/main/java/jcarbon/rapl:__pkg__"],
    linkshared = True,
    data = glob(["*.h"]),
    deps = [":jrapl-lib"],
)
