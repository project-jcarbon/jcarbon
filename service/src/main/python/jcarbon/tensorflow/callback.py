import os

from tensorflow.keras.callbacks import Callback

from jcarbon.client import JCarbonClient


class JCarbonCallback(Callback):
    def __init__(self, addr='localhost:8980', period_ms=None, output_dir=None):
        self.pid = os.getpid()
        self.period_ms = period_ms
        self.client = JCarbonClient(addr)
        self.data = []
        self.output_dir = output_dir
        self.i = 0

    def on_epoch_begin(self, epoch, logs = None):
        self.client.start(self.pid, self.period_ms)

    def on_epoch_end(self, epoch, logs = None):
        self.client.stop(self.pid)
        self.client.dump(self.pid, os.path.join(
            self.output_dir, f'jcarbon-{self.pid}-{self.i}.json'))
        self.i += 1
