# Script to reproduce the energy accounting experiments with dacapo

DATA_DIR=data
mkdir -p "${DATA_DIR}"

ITERATIONS=20
LOCALE=USA

run_benchmark() {
    local data_dir="${DATA_DIR}/${BENCHMARK}"
    mkdir -p "${data_dir}"
    java \
        -Djcarbon.benchmarks.output="${data_dir}" \
        -Djcarbon.emissions.locale="${LOCALE}" \
        -jar bazel-bin/benchmarks/src/main/java/jcarbon/benchmarks/dacapo_deploy.jar \
        -n ${ITERATIONS} --no-validation \
        -c jcarbon.benchmarks.JCarbonCallback \
        ${BENCHMARK} -s ${SIZE}
}

# default size dacapo benchmarks
BENCHMARKS=(
    biojava
    cassandra
    fop
    h2o
    jme
    jython
    kafka
    luindex
    lusearch
    tradebeans
    tradesoap
    xalan
    zxing
)

SIZE=default

for BENCHMARK in ${BENCHMARKS[@]}; do
    run_benchmark
done

# large size dacapo benchmarks
BENCHMARKS=(
    avrora
    batik
    eclipse
    # TODO: need to update and setup the new dacapo with the big data
    # graphchi
    h2
    pmd
    sunflow
    tomcat
)

SIZE=large

for BENCHMARK in ${BENCHMARKS[@]}; do
    run_benchmark
done

# huge size dacapo benchmarks
# TODO: need to update and setup the new dacapo with the big data
# BENCHMARKS=(
#     graphchi
# )

# SIZE=huge

# for BENCHMARK in ${BENCHMARKS[@]}; do
#     run_benchmark
# done
