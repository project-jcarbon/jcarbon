from concurrent import futures
from multiprocessing import Pipe
from time import sleep, time

import grpc

from jcarbon.jcarbon_service_pb2 import ReadResponse, StartResponse, StopResponse
from jcarbon.jcarbon_service_pb2_grpc import JCarbonService, add_JCarbonServiceServicer_to_server
from jcarbon.nvml.sampler import create_report, NvmlSampler


PARENT_PIPE, CHILD_PIPE = Pipe()


def run_sampler(period):
    sampler = NvmlSampler()
    while not CHILD_PIPE.poll():
        start = time()
        sampler.sample()
        elapsed = time() - start
        remaining = period - elapsed
        if (remaining > 0):
            sleep(remaining)
    return sampler.data


class JCarbonNvmlService(JCarbonService):
    def __init__(self):
        self.is_running = False
        self.report = None
        self.executor = futures.ThreadPoolExecutor(1)

    def Start(self, request, context):
        if not self.is_running:
            print('starting sampling')
            self.is_running = True
            self.sampling_future = self.executor.submit(
                run_sampler, request.period_millis / 1000.0)
        return StartResponse()

    def Stop(self, request, context):
        if self.is_running:
            print('stopping sampling')
            PARENT_PIPE.send(1)
            self.report = create_report(self.sampling_future.result())
            CHILD_PIPE.recv()
            self.sampling_future = None
            self.is_running = False
        return StopResponse()

    def Read(self, request, context):
        print('reading report')
        return ReadResponse(report=self.report)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_JCarbonServiceServicer_to_server(JCarbonNvmlService(), server)
    server.add_insecure_port("localhost:8981")
    print('starting jcarbon nvml server at localhost:8981')
    server.start()
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
