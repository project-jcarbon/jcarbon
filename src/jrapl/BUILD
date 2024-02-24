java_library(
    name = "jrapl",
    srcs = glob([
        "src/main/java/jrapl/*.java",
    ])
)

java_binary(
    name = "smoke_test",
    main_class = "jrapl.SmokeTest",
    runtime_deps = [":jrapl"],
)
