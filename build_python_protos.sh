python3 -m grpc_tools.protoc \
    -Iservice/src/main/proto/jcarbon/service \
    -Isrc/jcarbon-proto/src/main/proto/jcarbon/signal \
    --python_out=service/src/main/python/jcarbon \
    --pyi_out=service/src/main/python/jcarbon \
    --grpc_python_out=service/src/main/python/jcarbon \
    src/jcarbon-proto/src/main/proto/jcarbon/signal/signal.proto \
    service/src/main/proto/jcarbon/service/jcarbon_service.proto
