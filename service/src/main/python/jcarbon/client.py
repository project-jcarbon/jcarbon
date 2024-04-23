""" a client that can talk to an smaragdine JCarbon. """
import grpc

from jcarbon.jcarbon_service_pb2 import DumpRequest, ReadRequest, StartRequest, StopRequest
from jcarbon.jcarbon_service_pb2_grpc import JCarbonServiceStub

DEFAULT_PERIOD_MS = 10


class JCarbonClient:
    def __init__(self, addr):
        self.stub = JCarbonServiceStub(grpc.insecure_channel(addr))

    def start(self, pid, period=None):
        if period is not None:
            self.stub.Start(StartRequest(process_id=pid, period_millis=period))
        else:
            self.stub.Start(StartRequest(
                process_id=pid, period_millis=DEFAULT_PERIOD_MS))

    def stop(self, pid):
        self.stub.Stop(StopRequest(process_id=pid))

    def dump(self, pid, output_path):
        self.stub.Dump(DumpRequest(process_id=pid, output_path=output_path))

    def read(self, pid, signals):
        return self.stub.Read(ReadRequest(process_id=pid, signals=signals))
