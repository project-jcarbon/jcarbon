import os

from tensorflow.estimator import SessionRunHook

from jcarbon.client import JCarbonClient


class JCarbonHook(SessionRunHook):
    def __init__(self, addr='localhost:8980', period_ms=None, output_dir=None):
        self.pid = os.getpid()
        self.period_ms = period_ms
        self.client = JCarbonClient(addr)
        self.data = []
        self.output_dir = output_dir
        self.i = 0

    def before_run(self, run_context):
        self.client.start(self.pid, self.period_ms)

    def after_run(self, run_context, run_values):
        self.client.stop(self.pid)
        self.client.dump(self.pid, os.path.join(
            self.output_dir, f'jcarbon-{self.pid}-{self.i}.json'))
        self.i += 1

    def end(self, session):
        self.client.stop(self.pid)
