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


EXCLUDED_OPS = ['_SOURCE', 'cuStreamSynchronize']


# TODO: there's a way to parallelize the footprint generation if we can shape it
# as {'start': ts, 'end': ts, 'ops': set} here
def flatten_flow(trace):
    segments = {}
    # for each event, add the event op to the entry of its start timestamp and
    # the exit of its end timestamp
    for event in trace:
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


# code to generate footprints
def generate_footprint(flow, power):
    energy = {}
    runtime = {}

    # TODO: sigh. one day i'll figure out how to write this optimally
    flow_ts = list(flow)
    flow_ts.sort()
    t_i = 0

    power_ts = list(power)
    power_ts.sort()
    p_i = 0

    ts = list(set(power_ts + flow_ts))
    ts.sort()

    start, end = min(flow_ts), max(flow_ts)
    ops = set()
    i = ts.index(start)

    while start < end:
        current = ts[i + 1]
        while p_i != len(power_ts) - 1 and power_ts[p_i + 1] < current:
            p_i += 1
        while t_i != len(flow_ts) - 1 and flow_ts[t_i + 1] < current:
            t_i += 1

        events = flow[flow_ts[t_i]]
        ops |= events['entry']
        if len(ops) != 0:
            # units from the trace are in micros but power is in watts
            r = (current - start) / 1000000
            p = power[power_ts[p_i]]
            for op in ops:
                if op not in energy:
                    energy[op] = 0
                    runtime[op] = 0
                runtime[op] += r
                energy[op] += p * r / len(ops)
            ops -= events['exit']

        i += 1
        start = current
    return {'energy': energy, 'runtime': runtime}
