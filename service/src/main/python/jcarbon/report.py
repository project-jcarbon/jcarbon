import argparse
import os

import pandas as pd

from jcarbon.signal_pb2 import Report, Signal


def normalize_timestamps(timestamps, bucket_size_ms):
    """ normalizes ns timestamps to ms-bucketed timestamps """
    # TODO: this is producing strange behavior due to int division:
    #   2938450289096200 // 10**6 = 2938450288
    # TODO: taken from vesta's source. need to determine how to merge
    return bucket_size_ms * (timestamps // 10**6 // bucket_size_ms)


def to_dataframe(report, signals=None):
    signals = []
    monotonic_time = None
    for component in report.component:
        for signal in component.signal:
            for interval in signal.interval:
                l = []
                start = 1000000000 * interval.start.secs + interval.start.nanos
                end = 1000000000 * interval.end.secs + interval.end.nanos
                for data in interval.data:
                    l.append([component.component_type, component.component_id,
                              Signal.Unit.DESCRIPTOR.values_by_number[signal.unit].name,
                              ';'.join(list(signal.source)),
                              start,
                              end,
                              ';'.join(
                                  [f'{metadata.name}={metadata.value}' for metadata in data.metadata]),
                              data.value,
                              ])
                signals.append(pd.DataFrame(data=l, columns=[
                    'component_type', 'component_id', 'unit', 'source', 'start', 'end', 'metadata', 'value'
                ]))

    signals = pd.concat(signals)
    signals['start'] = pd.to_datetime(signals.start, unit='ns')
    signals['end'] = pd.to_datetime(signals.end, unit='ns')

    return signals.set_index(['component_type', 'component_id', 'unit', 'source', 'start', 'end', 'metadata']).value.sort_index()


def parse_args():
    parser = argparse.ArgumentParser(description='vesta probe monitor')
    parser.add_argument(
        nargs='*',
        type=str,
        help='jcarbon report protos',
        dest='files',
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
        signal_file = f'{os.path.splitext(file)[0]}.csv'
        print(f'writing to {signal_file}')
        signals.to_csv(signal_file)


if __name__ == '__main__':
    main()
