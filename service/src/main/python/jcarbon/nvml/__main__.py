from argparse import ArgumentParser
from time import sleep, time

from psutil import pid_exists

from jcarbon.nvml.sampler import NvmlSampler, create_report


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
    report = create_report(sampler.samples)
    energy = {}
    for component in report.component:
        for signal in component.signal:
            energy[','.join(signal.source)] = sum(
                data.value for interval in signal.interval for data in interval.data
            )
    print(energy)


if __name__ == '__main__':
    main()
