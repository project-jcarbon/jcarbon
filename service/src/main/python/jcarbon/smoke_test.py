""" a client that can talk to an jcarbon server. """
from argparse import ArgumentParser
from os import getpid
from pprint import pprint

from jcarbon.client import JCarbonClient


def fib(n):
    if n == 0 or n == 1:
        return 1
    else:
        return fib(n - 1) + fib(n - 2)


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser('jcarbon smoke tester')
    parser.add_argument(
        '-n',
        '--number',
        dest='n',
        type=int,
        default=42,
        help='fibonacci number to compute',
    )
    parser.add_argument(
        '--addr',
        dest='addr',
        type=str,
        default='localhost:8980',
        help='address of the smaragdine server',
    )
    parser.add_argument(
        '-s',
        '--signals',
        dest='signals',
        type=str,
        default='jcarbon.emissions.Emissions',
        help='signals to read from jcarbon',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    signals = args.signals.split(',')
    pid = getpid()

    print(f'smoke testing server at {args.addr} with fib({args.n})')
    client = JCarbonClient(args.addr)
    client.start(pid)
    fib(args.n)
    client.stop(pid)
    report = client.read(pid, signals)
    # TODO: this is too vague
    results = {}
    for jcarbon_signal in report.signal:
        results[jcarbon_signal.signal_name] = {}
        for signal in jcarbon_signal.signal:
            key = (signal.component, signal.unit)
            if key not in results[jcarbon_signal.signal_name]:
                results[jcarbon_signal.signal_name][key] = 0
            results[jcarbon_signal.signal_name][key] += sum(
                data.value for data in signal.data)
    pprint(results)


if __name__ == '__main__':
    main()
