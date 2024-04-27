from time import time

from pynvml import nvmlInit, nvmlDeviceGetCount
from pynvml import nvmlDeviceGetHandleByIndex, nvmlDeviceGetName
from pynvml import nvmlDeviceGetTotalEnergyConsumption, nvmlDeviceGetPowerUsage

from jcarbon.jcarbon_service_pb2 import JCarbonReport, JCarbonSignal, Signal


def get_timestamp():
    timestamp = time()
    secs = int(timestamp)
    nanos = int(1000000000 * (timestamp - secs))
    return {'secs': secs, 'nanos': nanos}


DEFAULT_SIGNALS = [
    'jcarbon.nvml.NvmlTotalEnergy',
    'jcarbon.nvml.NvmlEstimatedEnergy'
]


class NvmlSampler:
    def __init__(self, signals=DEFAULT_SIGNALS):
        self.devices_handles = []
        self.devices_names = []
        self.signals = signals
        self.data = {}
        for signal in self.signals:
            self.data[signal] = []
        try:
            nvmlInit()
            for i in range(nvmlDeviceGetCount()):
                self.devices_handles.append(nvmlDeviceGetHandleByIndex(i))
                self.devices_names.append(
                    nvmlDeviceGetName(self.devices_handles[i]))
        except:
            # TODO: silently fail for now
            pass

    def sample(self):
        timestamp = get_timestamp()
        for signal in self.signals:
            if 'NvmlTotalEnergy' in signal:
                unit = 'JOULES'
            elif 'NvmlEstimatedEnergy' in signal:
                unit = 'WATTS'
            else:
                unit = ''
            sample = {
                'timestamp': timestamp,
                'unit': unit,
                'data': []}
            for i, handle in enumerate(self.devices_handles):
                if 'NvmlTotalEnergy' in signal:
                    sample['data'].append({
                        'component': f'device={i},name={self.devices_names[i]}',
                        'value': nvmlDeviceGetTotalEnergyConsumption(handle) / 1000.0,
                    })
                elif 'NvmlEstimatedEnergy':
                    sample['data'].append({
                        'component': f'device={i},name={self.devices_names[i]}',
                        'value': nvmlDeviceGetPowerUsage(handle) / 1000.0,
                    })
            self.data[signal].append(sample)

    def get_report(self):
        report = JCarbonReport()
        for signal_name in self.data:
            jcarbon_signal = JCarbonSignal()
            jcarbon_signal.signal_name = signal_name
            for first, second in zip(self.data[signal_name], self.data[signal_name][1:]):
                jcarbon_signal.signal.append(sample_difference(first, second))
            report.signal.append(jcarbon_signal)
        self.data = {}
        for signal in self.signals:
            self.data[signal] = []
        return report


def sample_difference(first, second):
    signal = Signal()

    signal.start.secs = first['timestamp']['secs']
    signal.start.nanos = first['timestamp']['nanos']
    signal.end.secs = second['timestamp']['secs']
    signal.end.nanos = second['timestamp']['nanos']

    signal.component = 'nvml'
    signal.unit = 'JOULES'
    if first['unit'] == 'WATTS':
        elapsed = 1000000000.0 * (signal.end.secs - signal.start.secs)
        if signal.start.nanos > signal.end.nanos:
            elapsed += 1000000000.0 + signal.end.nanos - signal.start.nanos
            if elapsed >= 1000000000.0:
                elapsed -= 1000000000.0
        else:
            elapsed += signal.end.nanos - signal.start.nanos
        elapsed /= 1000000000.0

    f = {data['component']: data for data in first['data']}
    s = {data['component']: data for data in second['data']}
    for component, data in f.items():
        if component in s:
            other = s[component]
        signal_data = Signal.Data()

        signal_data.component = component
        if first['unit'] == 'JOULES':
            signal_data.value = other['value'] - data['value']
        elif first['unit'] == 'WATTS':
            signal_data.value = data['value'] * elapsed
        signal.data.append(signal_data)
    return signal
