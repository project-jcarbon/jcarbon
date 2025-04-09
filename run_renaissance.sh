# Script to reproduce the energy accounting experiments with renaissance

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
        -jar bazel-bin/benchmarks/src/main/java/jcarbon/benchmarks/renaissance_deploy.jar \
        -r ${ITERATIONS} \
        --plugin "!jcarbon.benchmarks.JCarbonPlugin" \
        ${BENCHMARK}
        java -jar bazel-bin/src/jcarbon-proto/sys_thermal_cooldown_deploy.jar -period 10000 -temperature 35
}

BENCHMARKS=(
    scrabble
    page-rank
    future-genetic
    akka-uct
    movie-lens
    scala-doku
    chi-square
    fj-kmeans
    rx-scrabble
    db-shootout
    neo4j-analytics
    finagle-http
    reactors
    dec-tree
    scala-stm-bench7
    naive-bayes
    als
    par-mnemonics
    scala-kmeans
    philosophers
    log-regression
    gauss-mix
    mnemonics
    dotty
    finagle-chirper
)

for BENCHMARK in ${BENCHMARKS[@]}; do
    run_benchmark
done
