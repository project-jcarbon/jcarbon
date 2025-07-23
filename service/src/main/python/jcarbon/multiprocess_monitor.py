import argparse
import os

from re import search
from time import time

import psutil
import pandas as pd

from jcarbon.client import JCarbonClient
from jcarbon.report import to_dataframe

DEFAULT_SIGNALS = [
    'linux_process',
    'linux_system',
    'ACTIVITY',
    'JOULES',
    'GRAMS_OF_CO2',
    'CELSIUS',
    'HERTZ',
]


def parse_args():
    parser = argparse.ArgumentParser(
        description='multiprocessed jcarbon monitor for pyperformance')
    parser.add_argument('-p', '--pid', type=int, help='pid to monitor')
    parser.add_argument(
        '--addr',
        default='localhost:8980',
        type=str,
        help='grpc server address',
    )
    parser.add_argument(
        '--output',
        type=str,
        help='location to write the log',
    )
    return parser.parse_args()


def is_child_process(child):
    child_pid = child.pid
    if not psutil.pid_exists(child_pid):
        return None
    try:
        cmd = child.cmdline()
        if "pip" in cmd:
            return None
        elif len(cmd) == 0:
            return None
        else:
            return child_pid, '@'.join(cmd)
    except:
        return None


def get_child_process(process):
    while True:
        for child in process.children(recursive=False):
            child = is_child_process(child)
            if child is not None:
                return child


def monitor_process(pid, client):
    client.start(pid, 10)
    chunks = []
    last = time()
    while psutil.pid_exists(pid):
        if time() - last > 2:
            client.stop(pid)
            chunks.append(to_dataframe(client.read(
                pid,
                DEFAULT_SIGNALS
            )).to_frame())
            client.start(pid, 10)
            last = time()
    client.stop(pid)
    chunks.append(to_dataframe(client.read(
        pid,
        DEFAULT_SIGNALS
    )).to_frame())
    return pd.concat(chunks)


def main():
    args = parse_args()

    process = psutil.Process(pid=args.pid)  # Main process to track
    print(f'monitoring process {args.pid}')

    client = JCarbonClient(args.addr)
    client.purge()
    i = 0
    try:
        # The psutil stuff is a hack for working with pyperformance
        while psutil.pid_exists(args.pid):
            child_pid, child_name = get_child_process(process)
            label = search(r'bm_([^/]+)', child_name).group(1)
            print(f'watching child {child_pid}: {child_name}')
            df = monitor_process(
                child_pid,
                client
            ).assign(benchmark=label)
            df.to_csv(os.path.join(args.output, f'jcarbon-{args.pid}-{i}.csv'))
            i += 1
            client.purge()
        print(f'pid {args.pid} terminated')
    except KeyboardInterrupt:
        print(f'monitoring of pid {args.pid} ended by user')


if __name__ == '__main__':
    main()
