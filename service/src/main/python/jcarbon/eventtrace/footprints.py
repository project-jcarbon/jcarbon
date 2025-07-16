from jcarbon.eventtrace import flatten_event_trace


# code to generate footprints
def from_event_trace(event_trace, power):
    energy = {}
    runtime = {}

    # TODO: sigh. one day i'll figure out how to write this optimally
    event_trace = flatten_event_trace(event_trace)
    event_ts = list(event_trace)
    event_ts.sort()
    event_idx = 0

    power_ts = list(power)
    power_ts.sort()
    power_idx = 0

    ts = list(set(power_ts + event_ts))
    ts.sort()

    start, end = min(event_ts), max(event_ts)
    ops = set()
    time_idx = ts.index(start)

    while start < end:
        current = ts[time_idx + 1]
        while power_idx != len(power_ts) - 1 and power_ts[power_idx + 1] < current:
            power_idx += 1
        while event_idx != len(event_ts) - 1 and event_ts[event_idx + 1] < current:
            event_idx += 1

        events = event_trace[event_ts[event_idx]]
        ops |= events['entry']
        if len(ops) != 0:
            # units from the trace are in micros but power is in watts
            # TODO: update once we have energy consumption from
            # thttps://docs.nvidia.com/deploy/nvml-api/group__nvmlDeviceQueries.html#group__nvmlDeviceQueries_1g732ab899b5bd18ac4bfb93c02de4900a
            r = (current - start) / 1000000
            p = power[power_ts[power_idx]]
            for op in ops:
                if op not in energy:
                    energy[op] = 0
                    runtime[op] = 0
                runtime[op] += r
                energy[op] += p * r / len(ops)
            ops -= events['exit']

        time_idx += 1
        start = current
    return {'energy': energy, 'runtime': runtime}
