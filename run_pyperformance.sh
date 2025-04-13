DATA_DIR=data
mkdir -p "${DATA_DIR}"

ITERATIONS=128
LOCALE=USA

run_benchmark() {
    local data_dir="${DATA_DIR}/${BENCHMARK}"
    mkdir -p "${data_dir}"
    python -m pyperformance run -b "${BENCHMARK}" > log.txt 2> log.txt &
    ROOT_PID=$!
    python -m jcarbon.multiprocess_monitor --pid ${ROOT_PID} --output ${data_dir}
    java -jar bazel-bin/src/jcarbon-proto/sys_thermal_cooldown_deploy.jar -period 10000 -temperature 35
}

# pyperfomance benchmarks
BENCHMARKS=(
    2to3
    async_generators
    async_tree
    async_tree_cpu_io_mixed
    async_tree_cpu_io_mixed_tg
    async_tree_eager
    async_tree_eager_cpu_io_mixed
    async_tree_eager_cpu_io_mixed_tg
    async_tree_eager_io
    async_tree_eager_io_tg
    async_tree_eager_memoization
    async_tree_eager_memoization_tg
    async_tree_eager_tg
    async_tree_io
    async_tree_io_tg
    async_tree_memoization
    async_tree_memoization_tg
    async_tree_tg
    asyncio_tcp
    asyncio_tcp_ssl
    asyncio_websockets
    chameleon
    chaos
    comprehensions
    concurrent_imap
    coroutines
    coverage
    crypto_pyaes
    dask
    deepcopy
    deltablue
    django_template
    docutils
    dulwich_log
    fannkuch
    float
    gc_collect
    gc_traversal
    generators
    genshi
    go
    hexiom
    html5lib
    json_dumps
    json_loads
    logging
    mako
    mdp
    meteor_contest
    nbody
    nqueens
    pathlib
    pickle
    pickle_dict
    pickle_list
    pickle_pure_python
    pidigits
    pprint
    pyflate
    python_startup
    python_startup_no_site
    raytrace
    regex_compile
    regex_dna
    regex_effbot
    regex_v8
    richards
    richards_super
    scimark
    spectral_norm
    sqlalchemy_declarative
    sqlalchemy_imperative
    sqlglot
    sqlglot_optimize
    sqlglot_parse
    sqlglot_transpile
    sqlite_synth
    sympy
    telco
    tomli_loads
    tornado_http
    typing_runtime_protocols
    unpack_sequence
    unpickle
    unpickle_list
    unpickle_pure_python
    xml_etree
)

SIZE=default

for BENCHMARK in ${BENCHMARKS[@]}; do
    run_benchmark
done
