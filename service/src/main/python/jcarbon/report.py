import argparse
import os

import pandas as pd

from jcarbon.jcarbon_service_pb2 import JCarbonReport


def normalize_timestamps(timestamps, bucket_size_ms):
    """ normalizes ns timestamps to ms-bucketed timestamps """
    # TODO: this is producing strange behavior due to int division:
    #   2938450289096200 // 10**6 = 2938450288
    # TODO: taken from vesta's source. need to determine how to merge
    return bucket_size_ms * (timestamps // 10**6 // bucket_size_ms)


def to_dataframe(report, signals=None):
    signals_df = []
    monotonic_time = None
    for jcarbon_signal in report.signal:
        if jcarbon_signal.signal_name == 'jcarbon.server.MonotonicTimestamp':
            # TODO: for now, i'm always grabbing the monotonic time.
            monotonic_time = {}
            for signal in jcarbon_signal.signal:
                start = 1000000000 * signal.start.secs + signal.start.nanos
                for data in signal.data:
                    monotonic_time[start] = data.value
            monotonic_time = pd.Series(monotonic_time)
            monotonic_time.index.name = 'timestamp'
            monotonic_time.name = 'ts'
        elif signals is None or jcarbon_signal.signal_name in signals:
            df = []
            for signal in jcarbon_signal.signal:
                start = 1000000000 * signal.start.secs + signal.start.nanos
                end = 1000000000 * signal.end.secs + signal.end.nanos
                for data in signal.data:
                    df.append([
                        jcarbon_signal.signal_name,
                        start,
                        end,
                        signal.component.replace(',', ':'),
                        signal.unit,
                        data.component.replace(',', ':'),
                        data.value,
                    ])
            signals_df.append(pd.DataFrame(data=df, columns=[
                              'signal', 'start', 'end', 'component', 'unit', 'subcomponent', 'value']))

    signals_df = pd.concat(signals_df)
    diff = (signals_df.end - signals_df.start).min() // 1000
    signals_df['start_norm'] = normalize_timestamps(signals_df.start, diff)
    if monotonic_time is not None:
        monotonic_time.index = normalize_timestamps(monotonic_time.index, diff)
        signals_df['ts'] = signals_df.start_norm.map(monotonic_time.to_dict())
    else:
        signals_df['ts'] = 0
    signals_df['start'] = pd.to_datetime(signals_df.start, unit='ns')
    signals_df['end'] = pd.to_datetime(signals_df.end, unit='ns')

    return signals_df.set_index(['signal', 'start', 'end', 'ts', 'component', 'unit', 'subcomponent']).value.sort_index()


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
        report = JCarbonReport()
        with open(file, 'rb') as f:
            report.ParseFromString(f.read())
        signals = to_dataframe(report)
        signal_file = f'{os.path.splitext(file)[0]}.csv'
        print(f'writing to {signal_file}')
        signals.to_csv(signal_file)


if __name__ == '__main__':
    main()
