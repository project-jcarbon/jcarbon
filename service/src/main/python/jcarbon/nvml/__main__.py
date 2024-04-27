from argparse import ArgumentParser
from pprint import pprint
from time import sleep, time

from psutil import pid_exists

from jcarbon.jcarbon_service_pb2 import JCarbonSignal
from jcarbon.nvml.sampler import NvmlSampler, sample_difference


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        '-p',
        '--period',
        dest='period',
        type=float,
        default=0.010,
        help='period to sample from the nvml',
    )
    parser.add_argument(
        '-t',
        '--time',
        dest='time',
        type=float,
        default=30,
        help='how long to sample from nvml',
    )
    parser.add_argument(
        '--pid',
        dest='pid',
        type=int,
        default=0,
        help='which process to wait to terminate',
    )
    parser.add_argument(
        '--output_path',
        dest='output_path',
        type=str,
        default='/tmp/jcarbon-nvml.json',
        help='location to write the report',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    sampler = NvmlSampler()
    try:
        start = time()
        while (args.pid < 1 or pid_exists(args.pid)) and time() - start < args.time:
            sample_start = time()
            sampler.sample()
            elapsed = time() - sample_start

            if args.period - elapsed > 0:
                sleep(args.period - elapsed)
    except KeyboardInterrupt:
        print('monitoring ended by user')
    report = sampler.get_report()
    energy = {}
    for jcarbon_signal in report.signal:
        signal_name = jcarbon_signal.signal_name
        for signal in jcarbon_signal.signal:
            for data in signal.data:
                if data.component not in energy:
                    energy[data.component] = []
                if signal_name not in energy[data.component]:
                    energy[data.component][signal_name].append(0)
                energy[data.component][signal_name][-1] += data.value
    print(energy)


if __name__ == '__main__':
    main()
