import json

from argparse import ArgumentParser
from math import sqrt
from pprint import pprint
from time import sleep, time

from tqdm import tqdm

from jcarbon.report import to_dataframe
from jcarbon.signal_pb2 import Signal
from jcarbon.nvml.sampler import NvmlSampler, SIGNALS


def equivalence_test(sleep_time, period):
    sampler = NvmlSampler()
    start = time()
    while time() - start < sleep_time:
        sample_start = time()
        sampler.sample()
        elapsed = time() - sample_start

        if period - elapsed > 0:
            sleep(period - elapsed)
    return sampler.create_report()


DEFAULT_PERIODS = [
    0.0001,
    0.001,
    0.010,
    0.100,
    1.000,
]

DEFAULT_WARMUP_ITERATIONS = 10
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
    pbar = tqdm(range(args.warm_up + args.iterations))
    for period in args.periods:
        pbar.reset()
        pbar.set_postfix()
        pbar.refresh()
        if period > args.time:
            continue
        if period > 1:
            period_fmt = f'{period:.4f} secs  '
        else:
            period_fmt = f'{(period * 1000.0):.4f} millis'
        pbar.set_description(f'[{period_fmt}]', refresh=True)

        reports = []
        for n in range(args.warm_up + args.iterations):
            pbar.update(1)
            try:
                report = equivalence_test(args.time, period)
                if n < args.warm_up:
                    continue
                energy = {}
                for jcarbon_signal in report.signal:
                    signal_name = jcarbon_signal.signal_name
                    for signal in jcarbon_signal.signal:
                        for data in signal.data:
                            key = signal_name.split('.')[-1][4:-6].lower()
                            if key not in energy:
                                energy[key] = 0
                            energy[key] += data.value
                pbar.set_postfix(
                    {key: f'{value:.4f}' for (key, value) in energy.items()})
                reports.append(report)
            except KeyboardInterrupt:
                print('equivalence test ended by user')
                return

        energy = {}
        for n, report in enumerate(reports):
            report = to_dataframe(report)
            energy = report[report.source == SIGNALS[Signal.Unit.JOULES]]
            elapsed = energy.reset_index('start').groupby(
                'metadata').start.agg(('min', 'max)'))
            energy = energy.groupby('metadata').sum() / \
                elapsed['max'] - elapsed['min']
            print(energy)
    pbar.close()


if __name__ == '__main__':
    main()
