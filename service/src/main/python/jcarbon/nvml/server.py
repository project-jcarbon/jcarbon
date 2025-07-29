import logging

from concurrent import futures
from multiprocessing import Pipe
from time import sleep, time

import grpc

from jcarbon.jcarbon_service_pb2 import ReadResponse, StartResponse, StopResponse, PurgeResponse
from jcarbon.jcarbon_service_pb2_grpc import JCarbonService, add_JCarbonServiceServicer_to_server
from jcarbon.nvml.sampler import NvmlSampler

MAX_MESSAGE_LENGTH = 20 * 1024 * 1024
PARENT_PIPE, CHILD_PIPE = Pipe()

logger = logging.getLogger(__name__)
logging.basicConfig(
    format="jcarbon-nvml-server (%(asctime)s) [%(name)s]: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S %p %Z",
    level=logging.DEBUG,
)


def run_sampler(period):
    sampler = NvmlSampler()
    while not CHILD_PIPE.poll():
        start = time()
        sampler.sample()
        elapsed = time() - start
        remaining = period - elapsed
        if (remaining > 0):
            sleep(remaining)
    return sampler.create_report()


class JCarbonNvmlService(JCarbonService):
    def __init__(self):
        self.is_running = False
        self.report = None
        self.executor = futures.ThreadPoolExecutor(1)
        self.sampling_future = None

    def Start(self, request, context):
        if not self.is_running:
            logger.info('starting sampling')
            self.is_running = True
            self.sampling_future = self.executor.submit(
                run_sampler,
                request.period_millis / 1000.0
            )
        else:
            logger.info(
                'ignoring start sampling request when already sampling')
        return StartResponse()

    def Stop(self, request, context):
        if self.is_running:
            logger.info('stop sampling')
            PARENT_PIPE.send(1)
            self.report = self.sampling_future.result()
            CHILD_PIPE.recv()
            self.sampling_future = None
            self.is_running = False
        else:
            logger.info('ignoring stop sampling request when not sampling')
        return StopResponse()

    def Read(self, request, context):
        logger.info('returning last report')
        # TODO: ignoring filtering because this should typically be small
        return ReadResponse(report=self.report)

    def Purge(self, request, context):
        logger.info('purging previous data')
        return PurgeResponse()


def serve():
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ('grpc.max_send_message_length', MAX_MESSAGE_LENGTH),
            ('grpc.max_receive_message_length', MAX_MESSAGE_LENGTH),
        ],
    )
    add_JCarbonServiceServicer_to_server(JCarbonNvmlService(), server)
    server.add_insecure_port("localhost:8981")
    logger.info('starting jcarbon nvml server at localhost:8981')
    server.start()
    server.wait_for_termination()
    logger.info('terminating...')


if __name__ == '__main__':
    serve()
