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
        '--temp',
        '--temperature',
        dest='temperature',
        type=float,
        default=40.00,
        help='temperature to start sampling',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    sampler = NvmlSampler()
    sample_temp = None
    start_temp = None
    end_temp = None
    max_temp = 0
    try:
        start = time()
        while(sample_temp is None or args.temperature < sample_temp):
            sample_start = time()
            sampler.sample()
            sample_temp = sampler.samples['nvmlDeviceGetTemperature'][-1]['data'][0]['value']
            max_temp = max(max_temp, sample_temp)
            if start_temp is None:
                start_temp = sample_temp
            end_temp = sample_temp
        elapsed = time() - start
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
    print(f'Experimented started at {start_temp} and reached next sampling temperature at {end_temp}')
    print(f'Max temperature reached was {max_temp}')
    print(f'This took {elapsed} seconds')


if __name__ == '__main__':
    main()
