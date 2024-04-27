""" a client that can talk to an jcarbon server. """
import json

from argparse import ArgumentParser
from os import getpid
from time import sleep

from psutil import pid_exists

from jcarbon.client import JCarbonClient


def fib(n):
    if n == 0 or n == 1:
        return 1
    else:
        return fib(n - 1) + fib(n - 2)


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='command',
        choices=['start', 'stop', 'read'],
        help='request to make',
    )
    parser.add_argument(
        '--pid',
        dest='pid',
        type=int,
        default=getpid(),
        help='pid to be monitored',
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
    parser.add_argument(
        '--output_path',
        dest='output_path',
        type=str,
        default='/tmp',
        help='location to write the report',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    signals = args.signals.split(',')
    if getpid() == args.pid:
        print('i refuse to watch myself!')
        return
    if any('jcarbon.cpu' in signal or 'jcarbon.emissions' for signal in signals):
        client = JCarbonClient(args.addr)
        client.start(args.pid)
    while pid_exists(args.pid):
        sleep(1)
    if any('jcarbon.cpu' in signal or 'jcarbon.emissions' for signal in signals):
        client.stop(args.pid)
        jcarbon_signal = client.read(args.pid, signals)
    print({
        signal.signal_name: sum(
            s.data.value for s in signal.signal) for signal in jcarbon_signal.signal})

if __name__ == '__main__':
    main()
