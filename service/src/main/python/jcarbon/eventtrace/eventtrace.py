# code to create minimal event traces (just enough to align) from the timeline
def get_inputs(e):
    inputs = []
    for key in e['args']:
        if 'input' in key:
            inputs.append(e['args'][key])
    if inputs:
        return '@' + ';'.join(inputs)
    else:
        return ''


def create_event(ts, dur, device, pid, op):
    return {
        'ts': ts,
        'dur': dur,
        'device': device,
        'pid': pid,
        'op': op
    }


def create_flow(events):
    return list(map(lambda e: create_event(**e), events))


def get_flow(timeline):
    metadata = [e for e in timeline['traceEvents'] if e['ph'] == 'M']
    devices = {}
    # TODO: find a way to map this more gracefully
    for device in metadata:
        device_name = device['args']['name']
        if ':GPU:' in device_name:
            devices[device['pid']] = 'GPU:' + \
                device_name.split(':GPU:')[1].split('/')[0].split(' ')[0]
        elif ':CPU:' in device_name:
            devices[device['pid']] = 'CPU:' + \
                device_name.split(':CPU:')[1].split('/')[0].split(' ')[0]

    # pull out all eXecution events and turn them into events
    events = create_flow({
        'ts': e['ts'],
        'dur': e['dur'],
        'device': devices[e['pid']],
        'pid': e['pid'],
        'op': e['args']['name'] + get_inputs(e)
    } for e in timeline['traceEvents'] if e['ph'] == 'X')
    return events


# code to segment (mark) traces
def create_segment():
    return {'entry': set(), 'exit': set()}


# TODO: i do not believe these operations actually represent logic. we
# need to dig into this when we get into tf2
EXCLUDED_OPS = ['_SOURCE', 'cuStreamSynchronize']


# TODO: there's a way to parallelize the footprint generation if we can shape it
# as {'start': ts, 'end': ts, 'ops': set(tensor name)} here
def flatten_event_trace(event_trace):
    segments = {}
    # for each event, add the event op to the entry of its start timestamp and
    # the exit of its end timestamp
    for event in event_trace:
        if event['op'] in EXCLUDED_OPS:
            continue
        if event['device'] not in segments:
            segments[event['device']] = {}

        trace = segments[event['device']]
        start = event['ts']
        end = event['ts'] + event['dur']
        if start not in trace:
            trace[start] = create_segment()
        if end not in trace:
            trace[end] = create_segment()
        trace[start]['entry'].add(event['op'])
        trace[end]['exit'].add(event['op'])
    return segments
