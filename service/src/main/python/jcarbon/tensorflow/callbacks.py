import os

from tensorflow.keras.callbacks import Callback

from jcarbon.client import JCarbonClient
from jcarbon.report import to_dataframe

DEFAULT_PERIOD_MS = 10
DEFAULT_SIGNALS = [
    'jcarbon.cpu.eflect.ProcessEnergy',
    'jcarbon.emissions.Emissions',
    'jcarbon.server.MonotonicTimestamp',
    'jcarbon.nvml.NvmlEstimatedEnergy',
    'jcarbon.nvml.NvmlTotalEnergy',
]


class JCarbonEpochCallback(Callback):
    def __init__(self, addr='localhost:8980', period_ms=DEFAULT_PERIOD_MS, signals=DEFAULT_SIGNALS):
        self.pid = os.getpid()
        self.period_ms = period_ms
        self.client = JCarbonClient(addr)
        self.signals = signals
        self.reports = []

    def on_epoch_begin(self, epoch, logs=None):
        self.client.start(self.pid, self.period_ms)

    def on_epoch_end(self, epoch, logs=None):
        self.client.stop(self.pid)
        self.reports.append(to_dataframe(
            self.client.read(self.pid, self.signals)))
        if logs is not None:
            for (signal, unit), df in self.reports[-1].groupby(['signal', 'unit']):
                logs[f'jcarbon-epoch-{signal}-{unit}'] = df.sum()


class JCarbonBatchCallback(Callback):
    def __init__(self, addr='localhost:8980', period_ms=DEFAULT_PERIOD_MS, signals=DEFAULT_SIGNALS):
        self.pid = os.getpid()
        self.period_ms = period_ms
        self.client = JCarbonClient(addr)
        self.signals = signals
        self.reports = []

    def on_train_batch_begin(self, epoch, logs=None):
        self.client.start(self.pid, self.period_ms)

    def on_train_batch_end(self, epoch, logs=None):
        self.client.stop(self.pid)
        self.reports.append(to_dataframe(
            self.client.read(self.pid, self.signals)))
        if logs is not None:
            for (signal, unit), df in self.reports[-1].groupby(['signal', 'unit']):
                logs[f'jcarbon-epoch-{signal}-{unit}'] = df.sum()
