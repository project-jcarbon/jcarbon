bazel build src/jcarbon-proto:sys_thermal_cooldown_deploy.jar
bazel build benchmarks:dacapo
bazel build benchmarks:renaissance
bash install_server_deps.sh
