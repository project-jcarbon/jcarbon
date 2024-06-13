import os
import time
import pandas as pd

from tensorflow.keras.callbacks import Callback

from jcarbon.client import JCarbonClient
from jcarbon.report import to_dataframe

DEFAULT_PERIOD_MS = 10
DEFAULT_PERIOD_SECS = 2
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
            for (signal, component, unit), df in self.reports[-1].groupby(['signal', 'component', 'unit']):
                # TODO: this should really not ignore negatives
                logs[f'jcarbon-epoch-{signal}-{component}-{unit}'] = df[df > 0].sum()


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
            for (signal, component, unit), df in self.reports[-1].groupby(['signal', 'component', 'unit']):
                # TODO: this should really not ignore negatives
                logs[f'jcarbon-epoch-{signal}-{component}-{unit}'] = df[df > 0].sum()


class JCarbonChunkingCallback(Callback):
    def __init__(
            self,
            addr='localhost:8980',
            period_ms=DEFAULT_PERIOD_MS,
            chunking_period_sec=DEFAULT_PERIOD_SECS,
            signals=DEFAULT_SIGNALS):
        self.pid = os.getpid()
        self.period_ms = period_ms
        self.chunking_period_sec = chunking_period_sec
        self.client = JCarbonClient(addr)
        self.signals = signals
        self.reports = []

    def on_epoch_begin(self, epoch, logs=None):
        self.time = time.time()
        self.chunks = []
        self.client.start(self.pid, self.period_ms)

    def on_train_batch_end(self, epoch, logs=None):
        curr = time.time()
        if (curr - self.time > self.chunking_period_sec):
            self.client.stop(self.pid)
            self.chunks.append(to_dataframe(
                self.client.read(self.pid, self.signals)))
            self.client.start(self.pid, self.period_ms)
            self.time = curr
            if logs is not None:
                for (signal, component, unit), df in self.chunks[-1].groupby(['signal', 'component', 'unit']):
                    # TODO: this should really not ignore negatives
                    logs[f'jcarbon-epoch-{signal}-{component}-{unit}'] = df[df > 0].sum()

    def on_epoch_end(self, epoch, logs=None):
        self.client.stop(self.pid)
        self.chunks.append(to_dataframe(
            self.client.read(self.pid, self.signals)))
        self.reports.append(pd.concat(self.chunks))
        if logs is not None:
            for (signal, component, unit), df in self.reports[-1].groupby(['signal', 'component', 'unit']):
                # TODO: this should really not ignore negatives
                logs[f'jcarbon-epoch-{signal}-{component}-{unit}'] = df[df > 0].sum()
