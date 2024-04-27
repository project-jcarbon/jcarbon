from argparse import ArgumentParser
from math import sqrt
from pprint import pprint
from time import sleep, time

from tqdm import tqdm

from jcarbon.jcarbon_service_pb2 import JCarbonSignal
from jcarbon.nvml.sampler import NvmlSampler, sample_difference


def summarize(nvml_signal):
    device_energy = {}
    faults = {}
    is_faulting = True
    for signal in nvml_signal.signal:
        for data in signal.data:
            energy = 0
            unit = data.unit
            if unit == 'JOULES':
                if data.value == 0:
                    fault_time = 1000000000 * signal.start.secs + signal.start.nanos
                    elapsed = 1000000000.0 * \
                        (signal.end.secs - signal.start.secs)
                    if signal.start.nanos > signal.end.nanos:
                        elapsed += 1000000000.0 + signal.end.nanos - signal.start.nanos
                        if elapsed >= 1000000000.0:
                            elapsed -= 1000000000.0
                    else:
                        elapsed += signal.end.nanos - signal.start.nanos
                    faults[fault_time] = (is_faulting, elapsed / 1000000000.0)
                    is_faulting = True
                else:
                    is_faulting = False
                    energy = data.value
            elif unit == 'WATTS':
                unit = 'WATTS_TO_JOULES'
                elapsed = 1000000000.0 * (signal.end.secs - signal.start.secs)
                if signal.start.nanos > signal.end.nanos:
                    elapsed += 1000000000.0 + signal.end.nanos - signal.start.nanos
                    if elapsed >= 1000000000.0:
                        elapsed -= 1000000000.0
                else:
                    elapsed += signal.end.nanos - signal.start.nanos
                energy = data.value * elapsed / 1000000000.0
            if (data.component, unit) not in device_energy:
                device_energy[data.component, unit] = 0
            device_energy[data.component, unit] += energy
    faults = [timestamp for (timestamp, faulted)
              in faults.items() if not faulted[0]]
    faults = [t2 - t1 for (t1, t2) in zip(faults, faults[1:])]
    return device_energy, faults


def get_energy(signal, data):
    energy = data.value
    unit = data.unit
    if unit == 'WATTS':
        unit = 'WATTS_TO_JOULES'
        elapsed = 1000000000.0 * (signal.end.secs - signal.start.secs)
        if signal.start.nanos > signal.end.nanos:
            elapsed += 1000000000.0 + signal.end.nanos - signal.start.nanos
            if elapsed >= 1000000000.0:
                elapsed -= 1000000000.0
        else:
            elapsed += signal.end.nanos - signal.start.nanos
        energy = energy * elapsed / 1000000000.0
    return energy, unit


def equivalence_test(sleep_time, period):
    sampler = NvmlSampler()
    start = time()
    while time() - start < sleep_time:
        sample_start = time()
        sampler.sample()
        elapsed = time() - sample_start

        if period - elapsed > 0:
            sleep(period - elapsed)
    return sampler.get_report()


DEFAULT_PERIODS = [
    0.001,
    0.002,
    0.005,
    0.010,
    0.020,
    0.050,
    0.100,
    0.200,
    0.500,
    1.000,
    2.000,
    5.000,
    10.00,
    15.00,
]

DEFAULT_WARMUP_ITERATIONS = 50
DEFAULT_DATA_ITERATIONS = 20
DEFAULT_TIME = 30.0
DEFAULT_OUTPUT = '/tmp/jcarbon-nvml-equivalence.json'


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        '--periods',
        dest='periods',
        type=list,
        default=DEFAULT_PERIODS,
        help='range of periods to test',
    )
    parser.add_argument(
        '-n',
        '--iterations',
        dest='iterations',
        type=int,
        default=DEFAULT_DATA_ITERATIONS,
        help='number of times to test sampling',
    )
    parser.add_argument(
        '-t',
        '--time',
        dest='time',
        type=float,
        default=DEFAULT_TIME,
        help='how long to sample from nvml',
    )
    parser.add_argument(
        '--warm-up',
        dest='warm_up',
        type=int,
        default=DEFAULT_WARMUP_ITERATIONS,
        help='number of initial iterations to discard',
    )
    parser.add_argument(
        '--output_path',
        dest='output_path',
        type=str,
        default=DEFAULT_OUTPUT,
        help='location to write the report',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    tests = {}
    for period in args.periods:
        if period > args.time:
            continue
        print(f'testing period of {period:.3f} seconds')
        reports = []
        for n in tqdm(range(args.warm_up + args.iterations)):
            try:
                report = equivalence_test(args.time, period)
                if n < args.warm_up:
                    continue
                reports.append(report)
            except KeyboardInterrupt:
                print('equivalence test ended by user')
                return

        energy = {}
        for n, report in enumerate(reports):
            for jcarbon_signal in report.signal:
                signal_name = jcarbon_signal.signal_name
                for signal in jcarbon_signal.signal:
                    for data in signal.data:
                        if data.component not in energy:
                            energy[data.component] = {}
                        if signal_name not in energy[data.component]:
                            energy[data.component][signal_name] = []
                        if n == len(energy[data.component][signal_name]):
                            energy[data.component][signal_name].append(0)
                        energy[data.component][signal_name][-1] += data.value
        for component in energy:
            for signal_name in energy[component]:
                data = energy[component][signal_name]
                count = len(data)
                mean = sum(data) / count
                std = sqrt(
                    sum((mean - value) ** 2 for value in data) / count)
                energy[component][signal_name] = f'{mean:.3f}+/-{std}'
        tests[period] = energy
    pprint(tests)


if __name__ == '__main__':
    main()
