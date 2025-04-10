import argparse
import os
import shutil

import numpy as np
import pandas as pd

from jcarbon.signal_pb2 import Report
from jcarbon.report import to_dataframe


def parse_args():
    parser = argparse.ArgumentParser(description='vesta probe monitor')
    parser.add_argument(
        nargs='*',
        type=str,
        help='jcarbon report protos',
        dest='files',
    )
    parser.add_argument(
        '--output_path',
        dest='output_path',
        default='/tmp',
        help='location to write the csv reports',
        type=str,
    )

    return parser.parse_args()


def main():
    args = parse_args()
    for file in args.files:
        print(f'converting {file}')
        report = Report()
        with open(file, 'rb') as f:
            report.ParseFromString(f.read())
        signals = to_dataframe(report)
        signal_file = os.path.join(
            args.output_path, f'{os.path.splitext(file)[0]}.csv')
        try:
            data_dir = os.path.dirname(signal_file)
            os.makedirs(data_dir)
        except Exception as e:
            print(e)
        print(f'writing to {signal_file}')
        signals.to_csv(signal_file)


if __name__ == '__main__':
    main()
