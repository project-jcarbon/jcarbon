""" a client that can talk to an jcarbon server. """
import json

from argparse import ArgumentParser
from os import getpid
from time import sleep

from jcarbon.client import JCarbonClient


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='command',
        choices=['start', 'stop', 'read', 'smoke_test',
                 'test', 'smoke-test', 'smoketest'],
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
        '--output_path',
        dest='output_path',
        type=str,
        default='/tmp',
        help='location to write the report',
    )
    return parser.parse_args()


def main():
    args = parse_args()

    client = JCarbonClient(args.addr)
    if args.command == 'start':
        if args.pid < 0:
            raise Exception(
                'invalid pid to monitor ({})'.format(args.pid))
        client.start(args.pid)
    elif args.command == 'stop':
        client.stop(args.pid)
    elif args.command == 'read':
        client.dump(args.pid, args.output_path)
    elif args.command in ['smoke_test', 'test', 'smoke-test', 'smoketest']:
        client.start(args.pid)
        sleep(1)
        client.stop(args.pid)
        file_path = '/tmp/jcarbon-smoke_test-{args.pid}.json'
        client.dump(args.pid, '/tmp/jcarbon-smoke_test-{args.pid}.json')
        with open(file_path) as f:
            report = json.loads(f)
        print(report)


if __name__ == '__main__':
    main()
