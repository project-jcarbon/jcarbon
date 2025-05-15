from time import time

from pynvml import nvmlInit, nvmlDeviceGetCount
from pynvml import nvmlDeviceGetHandleByIndex, nvmlDeviceGetName
from pynvml import nvmlDeviceGetTotalEnergyConsumption, nvmlDeviceGetPowerUsage, nvmlDeviceGetTemperature, nvmlDeviceGetClock
from pynvml import NVML_TEMPERATURE_GPU, NVML_CLOCK_GRAPHICS, NVML_CLOCK_MEM, NVML_CLOCK_ID_CURRENT, NVML_CLOCK_ID_APP_CLOCK_TARGET


from jcarbon.signal import JCarbonSignal, sample_beginning, sample_difference
from jcarbon.signal_pb2 import Report, Component, Signal


def sample_from(timestamp, device_handles, source):
    data = []
    for i, handle in enumerate(device_handles):
        data.append({
            'metadata': [{'name': 'device', 'value': str(i)}],
            'value': source(handle)
        })
    return {
        'timestamp': timestamp,
        'data': data,
    }


def get_nvml_energy(handle):
    return nvmlDeviceGetTotalEnergyConsumption(handle) / 1000.0


class NvmlEnergySignal(JCarbonSignal):
    def __init__(self, device_handles):
        super().__init__()
        self.device_handles = device_handles

    @property
    def name(self):
        return 'nvmlDeviceGetTotalEnergyConsumption'

    def sample(self, timestamp):
        self.samples.append(sample_from(
            timestamp,
            self.device_handles,
            get_nvml_energy,
        ))

    def diff(self):
        signal = Signal()
        signal.unit = Signal.Unit.JOULES
        for first, second in zip(self.samples, self.samples[1:]):
            signal.interval.append(sample_difference(first, second))
        signal.source.append(self.name)
        return signal


def get_nvml_power(handle):
    return nvmlDeviceGetPowerUsage(handle) / 1000.0


class NvmlPowerSignal(JCarbonSignal):
    def __init__(self, device_handles):
        super().__init__()
        self.device_handles = device_handles

    @property
    def name(self):
        return 'nvmlDeviceGetPowerUsage'

    def sample(self, timestamp):
        self.samples.append(sample_from(
            timestamp,
            self.device_handles,
            get_nvml_power,
        ))

    def diff(self):
        signal = Signal()
        signal.unit = Signal.Unit.WATTS
        for first, second in zip(self.samples, self.samples[1:]):
            signal.interval.append(sample_beginning(first, second))
        signal.source.append(self.name)
        return signal


def get_nvml_temperature(handle):
    return nvmlDeviceGetTemperature(handle, NVML_TEMPERATURE_GPU)


class NvmlTemperatureSignal(JCarbonSignal):
    def __init__(self, device_handles):
        super().__init__()
        self.device_handles = device_handles

    @property
    def name(self):
        return 'nvmlDeviceGetTemperature'

    def sample(self, timestamp):
        self.samples.append(sample_from(
            timestamp,
            self.device_handles,
            get_nvml_temperature,
        ))

    def diff(self):
        signal = Signal()
        signal.unit = Signal.Unit.CELSIUS
        for first, second in zip(self.samples, self.samples[1:]):
            signal.interval.append(sample_beginning(first, second))
        signal.source.append(self.name)
        return signal


def get_nvml_clock_app_target(handle):
    return nvmlDeviceGetClock(handle, NVML_CLOCK_GRAPHICS, NVML_CLOCK_ID_APP_CLOCK_TARGET) * 10**6


def get_nvml_clock_current(handle):
    return nvmlDeviceGetClock(handle, NVML_CLOCK_GRAPHICS, NVML_CLOCK_ID_CURRENT) * 10**6,


class NvmlClockSignal(JCarbonSignal):
    def __init__(self, device_handles):
        super().__init__()
        self.device_handles = device_handles

    @property
    def name(self):
        return 'nvmlDeviceGetClock'

    def sample(self, timestamp):
        sample = sample_from(
            timestamp,
            self.device_handles,
            get_nvml_clock_app_target,
        )
        for data in sample['data']:
            data['metadata'].append(
                {'name': 'clockType', 'value': "NVML_CLOCK_GRAPHICS"},
                {'name': 'clockId',
                 'value': "NVML_CLOCK_ID_APP_CLOCK_TARGET"},
            )
        self.samples.append(sample)

        sample = sample_from(
            timestamp,
            self.device_handles,
            get_nvml_clock_current,
        )
        for data in sample['data']:
            data['metadata'].append(
                {'name': 'clockType', 'value': "NVML_CLOCK_GRAPHICS"},
                {'name': 'clockId',
                 'value': "NVML_CLOCK_ID_CURRENT"},
            )
        self.samples.append(sample)

    def diff(self):
        signal = Signal()
        signal.unit = Signal.Unit.HERTZ
        for first, second in zip(self.samples, self.samples[1:]):
            signal.interval.append(sample_beginning(first, second))
        signal.source.append(self.name)
        return signal


SIGNALS = {
    Signal.Unit.JOULES: 'nvmlDeviceGetTotalEnergyConsumption',
    Signal.Unit.WATTS: 'nvmlDeviceGetPowerUsage',
    Signal.Unit.CELSIUS: 'nvmlDeviceGetTemperature',
    Signal.Unit.HERTZ: 'nvmlDeviceGetClock',
}


def get_timestamp():
    timestamp = time()
    secs = int(timestamp)
    nanos = int(1000000000 * (timestamp - secs))
    return {'secs': secs, 'nanos': nanos}


class NvmlSampler:
    def __init__(self):
        devices_handles = []
        # self.device_metadata = []
        try:
            nvmlInit()
            for i in range(nvmlDeviceGetCount()):
                devices_handles.append(nvmlDeviceGetHandleByIndex(i))
                # TODO: this appears to fail on some systems
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
        self.signals = [
            NvmlEnergySignal(devices_handles),
            NvmlPowerSignal(devices_handles),
            NvmlTemperatureSignal(devices_handles),
            NvmlClockSignal(devices_handles),
        ]

    def sample(self):
        timestamp = get_timestamp()
        for signal in self.signals:
            try:
                signal.sample(timestamp)
            except:
                pass
                # print(f'unable to sample from {signal.name}')

    def create_report(self):
        nvml_component = Component()
        nvml_component.component_type = 'nvml'
        nvml_component.component_id = ''
        for signal in self.signals:
            nvml_component.signal.append(signal.diff())
        report = Report()
        report.component.append(nvml_component)
        return report
