graceful_terminate() {
    echo "terminating servers gracefully"
}
trap 'graceful_terminate' SIGINT SIGTERM INT

python3 -m jcarbon.nvml.server &
nvml_pid=$!

java -jar bazel-bin/service/src/main/java/jcarbon/server/server_deploy.jar -nvml &
jcarbon_pid=$!

wait
python3 -m jcarbon.cli purge
kill -9 $jcarbon_pid $nvml_pid
