from google.protobuf.json_format import ParseDict

from jcarbon.signal_pb2 import Signal, SignalInterval


def sample_beginning(first_samples, second_samples):
    return ParseDict({
        'start': first_samples['timestamp'],
        'end': second_samples['timestamp'],
        'data': first_samples['data'],
    }, SignalInterval())


def sample_ending(first_samples, second_samples):
    return ParseDict({
        'start': first_samples['timestamp'],
        'end': second_samples['timestamp'],
        'data': second_samples['data'],
    }, SignalInterval())


def sample_difference(first_samples, second_samples):
    data = []
    for first, second in zip(first_samples['data'], second_samples['data']):
        data.append({
            'metadata': first['metadata'],
            'value': second['value'] - first['value']
        })
    return ParseDict({
        'start': first_samples['timestamp'],
        'end': second_samples['timestamp'],
        'data': data,
    }, SignalInterval())


class YucaSignal:
    @property
    def name(self):
        raise NotImplementedError('Yuca signals must have a name')

    @property
    def unit(self):
        raise NotImplementedError('Yuca signals must have a unit')

    def data(self):
        signal = Signal()
        signal.unit = self.unit
        for first, second in self.intervals():
            signal.interval.append(self.create_interval(first, second))
        signal.source.append(self.name)
        return signal

    def intervals(self):
        raise NotImplementedError(
            'Yuca signals must implement \'intervals()\'')

    def create_interval(self, first, second):
        raise NotImplementedError(
            'Yuca signals must implement \'create_interval()\'')