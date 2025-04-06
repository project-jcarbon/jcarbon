from time import time

from pynvml import nvmlInit, nvmlDeviceGetCount
from pynvml import nvmlDeviceGetHandleByIndex, nvmlDeviceGetName
from pynvml import nvmlDeviceGetTotalEnergyConsumption, nvmlDeviceGetPowerUsage, nvmlDeviceGetTemperature, nvmlDeviceGetClock
from pynvml import NVML_TEMPERATURE_GPU, NVML_CLOCK_GRAPHICS, NVML_CLOCK_MEM, NVML_CLOCK_ID_CURRENT, NVML_CLOCK_ID_APP_CLOCK_TARGET



from jcarbon.signal_pb2 import Report, Component, Signal, SignalInterval
from google.protobuf.json_format import ParseDict


def get_timestamp():
    timestamp = time()
    secs = int(timestamp)
    nanos = int(1000000000 * (timestamp - secs))
    return {'secs': secs, 'nanos': nanos}


DEFAULT_SIGNALS = [
    'nvmlDeviceGetTotalEnergyConsumption',
    'nvmlDeviceGetPowerUsage',
    'nvmlDeviceGetTemperature',
    'nvmlDeviceGetClock',
]

class NvmlSampler:
    def __init__(self, signals=DEFAULT_SIGNALS):
        self.devices_handles = []
        self.device_metadata = []
        self.signals = signals
        self.samples = {}
        for signal in self.signals:
            self.samples[signal] = []
        try:
            nvmlInit()
            for i in range(nvmlDeviceGetCount()):
                self.devices_handles.append(nvmlDeviceGetHandleByIndex(i))
                # self.device_metadata.append(
                #     {
                #         'device': i,
                #         'name': nvmlDeviceGetName(self.devices_handles[i])
                #     }
                # )
        except:
            # TODO: silently fail for now
            import traceback
            traceback.print_exc()
            pass

    def sample(self):
        timestamp = get_timestamp()
        for signal in self.signals:
            sample = {
                'timestamp': timestamp,
                'data': []}
            for i, handle in enumerate(self.devices_handles):
                if 'nvmlDeviceGetTotalEnergyConsumption' == signal:
                    sample['data'].append({
                        'metadata': [{'name': 'device', 'value': str(i)}],
                        'value': nvmlDeviceGetTotalEnergyConsumption(handle) / 1000.0,
                    })
                elif 'nvmlDeviceGetPowerUsage' == signal:
                    sample['data'].append({
                        'metadata': [{'name': 'device', 'value': str(i)}],
                        'value': nvmlDeviceGetPowerUsage(handle) / 1000.0,
                    })
                elif 'nvmlDeviceGetTemperature' == signal:
                    sample['data'].append({
                        'metadata': [{'name': 'device', 'value': str(i)}],
                        'value': nvmlDeviceGetTemperature(handle, NVML_TEMPERATURE_GPU),
                    })
                elif 'nvmlDeviceGetClock' == signal:
                    sample['data'].append({
                        'metadata': [
                            {'name': 'device', 'value': str(i)},
                            {'name': 'clockType', 'value': "NVML_CLOCK_GRAPHICS"},
                            {'name': 'clockId', 'value': "NVML_CLOCK_ID_APP_CLOCK_TARGET"},
                        ],
                        'value': nvmlDeviceGetClock(handle, NVML_CLOCK_GRAPHICS, NVML_CLOCK_ID_APP_CLOCK_TARGET) * 10**6,
                    })
                    sample['data'].append({
                        'metadata': [
                            {'name': 'device', 'value': str(i)},
                            {'name': 'clockType', 'value': "NVML_CLOCK_GRAPHICS"},
                            {'name': 'clockId', 'value': "NVML_CLOCK_ID_CURRENT"},
                        ],
                        'value': nvmlDeviceGetClock(handle, NVML_CLOCK_GRAPHICS, NVML_CLOCK_ID_CURRENT) * 10**6,
                    })
                    # TODO: don't need memory frequency yet
                    # sample['data'].append({
                    #     'metadata': [
                    #         {'name': 'device', 'value': i},
                    #         {'name': 'clockType', 'value': "NVML_CLOCK_MEM"},
                    #         {'name': 'clockId', 'value': "NVML_CLOCK_ID_APP_CLOCK_TARGET"},
                    #     ],
                    #     'value': nvmlDeviceGetClock(handle, NVML_CLOCK_MEM, NVML_CLOCK_ID_APP_CLOCK_TARGET) * 10**6,
                    # })
                    # sample['data'].append({
                    #     'metadata': [
                    #         {'name': 'device', 'value': i},
                    #         {'name': 'clockType', 'value': "NVML_CLOCK_MEM"},
                    #         {'name': 'clockId', 'value': "NVML_CLOCK_ID_CURRENT"},
                    #     ],
                    #     'value': nvmlDeviceGetClock(handle, NVML_CLOCK_MEM, NVML_CLOCK_ID_CURRENT) * 10**6,
                    # })
            self.samples[signal].append(sample)


def create_report(samples):
    nvml_component = Component()
    nvml_component.component_type = 'nvml'
    nvml_component.component_id = ''
    for signal_name in samples:
        signal = Signal()
        if 'nvmlDeviceGetTotalEnergyConsumption' == signal_name:
            signal.unit = Signal.Unit.JOULES
            for first, second in zip(samples[signal_name], samples[signal_name][1:]):
                signal.interval.append(sample_difference(first, second))
        elif 'nvmlDeviceGetPowerUsage' == signal_name:
            signal.unit = Signal.Unit.WATTS
            for first, second in zip(samples[signal_name], samples[signal_name][1:]):
                interval_dict = create_interval_dict(first, second)
                interval = ParseDict(interval_dict, SignalInterval())

                signal.interval.append(interval)
        elif 'nvmlDeviceGetTemperature' == signal_name:
            signal.unit = Signal.Unit.CELSIUS
            for first, second in zip(samples[signal_name], samples[signal_name][1:]):
                interval_dict = create_interval_dict(first, second)
                interval = ParseDict(interval_dict, SignalInterval())

                signal.interval.append(interval)
        elif 'nvmlDeviceGetClock' == signal_name:
            signal.unit = Signal.Unit.HERTZ
            for first, second in zip(samples[signal_name], samples[signal_name][1:]):
                interval_dict = create_interval_dict(first, second)
                interval = ParseDict(interval_dict, SignalInterval())

                signal.interval.append(interval)
        else:
            continue
        signal.source.append(signal_name)

        nvml_component.signal.append(signal)
    report = Report()
    report.component.append(nvml_component)
    return report


def sample_difference(first_samples, second_samples):
    interval = create_interval_with_timestamp(first_samples, second_samples)

    for first, second in zip(first_samples['data'], second_samples['data']):
        data = SignalInterval.SignalData()
        for meta in first['metadata']:
            metadata = SignalInterval.SignalData.Metadata()
            metadata.name = meta['name']
            metadata.value = str(meta['value'])
            data.metadata.append(metadata)
        data.value = second['value'] - first['value']
        interval.data.append(data)

    return interval

def create_interval_with_timestamp(first, second):
    interval = SignalInterval()

    interval.start.secs = first['timestamp']['secs']
    interval.start.nanos = first['timestamp']['nanos']
    interval.end.secs = second['timestamp']['secs']
    interval.end.nanos = second['timestamp']['nanos']

    return interval

def create_interval_dict(first, second):
    interval_dict = {
        'start': first['timestamp'],
        'end': second['timestamp'],
        'data': first['data'],
    }
    
    return interval_dict
