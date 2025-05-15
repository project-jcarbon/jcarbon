from google.protobuf.json_format import ParseDict

from jcarbon.signal_pb2 import SignalInterval


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


class JCarbonSignal:
    def __init__(self):
        self.samples = []

    @property
    def name(self):
        raise NotImplementedError('JCarbon signals must have a name')

    def sample(self, timestamp):
        raise NotImplementedError(
            'JCarbon signals must implement \'sample()\'')

    def diff(self):
        raise NotImplementedError('JCarbon signals must implement \'diff()\'')
