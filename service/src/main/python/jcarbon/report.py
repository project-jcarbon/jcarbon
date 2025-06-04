import pandas as pd

from jcarbon.signal_pb2 import Signal


def normalize_timestamps(timestamps, bucket_size_ms):
    """ normalizes ns timestamps to ms-bucketed timestamps """
    # TODO: this is producing strange behavior due to int division:
    #   2938450289096200 // 10**6 = 2938450288
    # TODO: taken from vesta's source. need to determine how to merge
    return bucket_size_ms * (timestamps // 10**6 // bucket_size_ms)


def to_dataframe(report, signals=None):
    signals = []
    for component in report.component:
        for signal in component.signal:
            for interval in signal.interval:
                start = 1000000000 * interval.start.secs + interval.start.nanos
                end = 1000000000 * interval.end.secs + interval.end.nanos
                for data in interval.data:
                    signals.append([
                        component.component_type,
                        component.component_id,
                        Signal.Unit.DESCRIPTOR.values_by_number[signal.unit].name,
                        signal.source[0],
                        start,
                        end,
                        ';'.join(
                            [f'{metadata.name}={metadata.value}' for metadata in data.metadata]),
                        data.value,
                    ])
    signals = pd.DataFrame(data=signals, columns=[
                           'component_type', 'component_id', 'unit', 'source', 'start', 'end', 'metadata', 'value'])
    # TODO: turning these off since it just makes the output bigger
    # signals['start'] = pd.to_datetime(signals.start, unit='ns')
    # signals['end'] = pd.to_datetime(signals.end, unit='ns')

    return signals.set_index(['component_type', 'component_id', 'unit', 'source', 'start', 'metadata', 'end']).value.sort_index()
