""" a cli to talk with a jcarbon server. """
from argparse import ArgumentParser
from os import getpid

from psutil import pid_exists

from jcarbon.client import JCarbonClient
from jcarbon.report import to_dataframe


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser('jcarbon cli')
    parser.add_argument(
        dest='command',
        choices=['start', 'stop', 'dump', 'read', 'purge'],
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
        '--period',
        dest='period',
        type=int,
        default=10,
        help='sampling period for \'start\'',
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
        help='signals to read from jcarbon for \'read\'',
    )
    parser.add_argument(
        '--output_path',
        dest='output_path',
        type=str,
        default='/tmp',
        help='location to write the report for \'dump\'',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    signals = args.signals.split(',')
    client = JCarbonClient(args.addr)
    if args.command == 'start':
        if args.pid < 0 or not pid_exists(args.pid):
            raise Exception(
                'invalid pid to monitor ({})'.format(args.pid))
        client.start(args.pid, args.period)
    elif args.command == 'stop':
        client.stop(args.pid)
    elif args.command == 'dump':
        client.dump(args.pid, args.output_path, signals)
    elif args.command == 'read':
        print(to_dataframe(client.read(args.pid, signals)))
    elif args.command == 'purge':
        print(client.purge())


if __name__ == '__main__':
    main()
