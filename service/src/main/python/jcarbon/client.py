""" a client that can talk to an smaragdine JCarbon. """
import grpc

from jcarbon.jcarbon_service_pb2 import DumpRequest, StartRequest, StopRequest
from jcarbon.jcarbon_service_pb2_grpc import JCarbonServiceStub


class JCarbonClient:
    def __init__(self, addr):
        self.stub = JCarbonServiceStub(grpc.insecure_channel(addr))

    def start(self, pid, period_ms=None):
        if period_ms is not None:
            self.stub.Start(StartRequest(pid=pid, period=period_ms))
        else:
            self.stub.Start(StartRequest(pid=pid))

    def stop(self, pid):
        self.stub.Stop(StopRequest(pid=pid))

    def dump(self, pid, output_path):
        self.stub.Dump(DumpRequest(pid=pid, output_path=output_path))
