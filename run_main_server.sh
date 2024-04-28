bazel build service/src/main/java/jcarbon/server:server_deploy.jar
sudo java -jar bazel-bin/service/src/main/java/jcarbon/server/server_deploy.jar -nvml
