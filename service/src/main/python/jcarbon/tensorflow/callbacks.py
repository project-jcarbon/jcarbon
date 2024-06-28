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

UNITS = {
    'GRAMS_OF_CO2': 'CO2',
    'JOULES': 'J',
    'ACTIVITY': '%',
    'NANOSECONDS': 'ns',
    'JIFFIES': '',
    'WATTS': 'W',
}


def add_jcarbon_log(df, logs):
    for (component_type, component_id, unit, source), df in df.groupby(['component_type', 'component_id', 'unit', 'source']):
        # TODO: this should really not ignore negatives
        logs[f'{component_type}-{component_id}-{UNITS[unit]}'] = df[df > 0].sum()


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
        self._last_report = None
        self.client.start(self.pid, self.period_ms)

    def on_train_batch_end(self, epoch, logs=None):
        curr = time.time()
        if (curr - self.time > self.chunking_period_sec):
            self.client.stop(self.pid)
            report = self.client.read(self.pid, self.signals)
            if self._last_report is None:
                self._last_report = to_dataframe(report)
            self._last_report = pd.concat([
                self._last_report,
                to_dataframe(report)
            ])
            if logs is not None:
                add_jcarbon_log(self._last_report, logs)
            self.client.start(self.pid, self.period_ms)
            self.time = curr

    def on_epoch_end(self, epoch, logs=None):
        self.client.stop(self.pid)
        report = self.client.read(self.pid, self.signals)
        if self._last_report is None:
            self._last_report = to_dataframe(report)
        self._last_report = pd.concat([
            self._last_report,
            to_dataframe(report)
        ])
        if logs is not None:
            add_jcarbon_log(self._last_report, logs)
        self.reports.append(self._last_report)
