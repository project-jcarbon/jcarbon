""" a thin client to talk to a jcarbon server. """
import grpc

from jcarbon.jcarbon_service_pb2 import DumpRequest, PurgeRequest, ReadRequest, StartRequest, StopRequest
from jcarbon.jcarbon_service_pb2_grpc import JCarbonServiceStub


class JCarbonClient:
    def __init__(self, addr):
        self.stub = JCarbonServiceStub(grpc.insecure_channel(addr))

    def start(self, pid, period):
        self.stub.Start(StartRequest(process_id=pid, period_millis=period))

    def stop(self, pid):
        self.stub.Stop(StopRequest(process_id=pid))

    def dump(self, pid, output_path, signals):
        self.stub.Dump(DumpRequest(process_id=pid, output_path=output_path, signals=signals))

    def read(self, pid, signals):
        return self.stub.Read(ReadRequest(process_id=pid, signals=signals)).report

    def purge(self):
        return self.stub.Purge(PurgeRequest())
