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


def add_jcarbon_log(df, logs=None):
    if logs:
        for (component_type, component_id, unit, source), df in df.groupby(['component_type', 'component_id', 'unit', 'source']):
            # TODO: this should really not ignore negatives
            logs[f'{component_type}-{UNITS[unit]}'] = df[df > 0].sum()


class JCarbonCallback(Callback):
    def __init__(
            self,
            addr,
            period_ms=DEFAULT_PERIOD_MS,
            signals=DEFAULT_SIGNALS):
        self._pid = os.getpid()
        self._client = JCarbonClient(addr)
        self._period_ms = period_ms
        self._signals = signals

    def start_jcarbon(self):
        self._client.start(self.pid, self.period_ms)

    def _stop_jcarbon(self):
        self._client.stop(self.pid)

    def _get_report(self):
        return to_dataframe(self._client.read(self.pid, self.signals))

    def log_report(self, logs=None):
        self._stop_jcarbon()
        report = self._get_report()
        add_jcarbon_log(report, logs)
        return report


class JCarbonEpochCallback(JCarbonCallback):
    def __init__(self, addr='localhost:8980', period_ms=DEFAULT_PERIOD_MS, signals=DEFAULT_SIGNALS):
        super(addr, period_ms, signals)

    def on_epoch_begin(self, epoch, logs=None):
        self.start_jcarbon()

    def on_epoch_end(self, epoch, logs=None):
        self.reports.append(self._log_report(logs))


class JCarbonBatchCallback(JCarbonCallback):
    def __init__(self, addr='localhost:8980', period_ms=DEFAULT_PERIOD_MS, signals=DEFAULT_SIGNALS):
        super(addr, period_ms, signals)

    def on_train_batch_begin(self, epoch, logs=None):
        self.start_jcarbon()

    def on_train_batch_end(self, epoch, logs=None):
        self.reports.append(self._log_report(logs))


class JCarbonChunkingCallback(JCarbonCallback):
    def __init__(
            self,
            addr='localhost:8980',
            period_ms=DEFAULT_PERIOD_MS,
            signals=DEFAULT_SIGNALS,
            chunking_period_sec=DEFAULT_PERIOD_SECS):
        super(addr, period_ms, signals)
        self.chunking_period_sec = chunking_period_sec

    def on_epoch_begin(self, epoch, logs=None):
        self.time = time.time()
        self.last_report = None
        self.start_jcarbon()

    def on_train_batch_end(self, epoch, logs=None):
        curr = time.time()
        if (curr - self.time > self.chunking_period_sec):
            self.last_report.append(self._log_report())
            add_jcarbon_log(self.last_report, logs)
            self.time = curr
            self.start_jcarbon()

    def on_epoch_end(self, epoch, logs=None):
        self.last_report.append(self._log_report())
        add_jcarbon_log(self.last_report, logs)
        self.reports.append(self.last_report)

    def add_to_report(self, report=None):
        if report is None:
            return report
        else:
            return pd.concat([self.last_report, report])
