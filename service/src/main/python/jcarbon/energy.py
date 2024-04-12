""" processing and virtualization code for a DataSet using pandas """
import os

from argparse import ArgumentParser
from zipfile import ZipFile

import numpy as np
import pandas as pd
import psutil

from pandas import to_datetime

from smaragdine.protos.sample_pb2 import DataSet

# processing helpers
INTERVAL = '4ms'
WINDOW_SIZE = '101ms'


def bucket_timestamps(timestamps, interval=INTERVAL):
    """ Floors a series of timestamps to some interval for easy aggregates. """
    return to_datetime(timestamps).dt.floor(interval)


def max_rolling_difference(df, window_size=WINDOW_SIZE):
    """ Computes a rolling difference of points up to the window size. """
    values = df - df.rolling(window_size).min()

    timestamps = df.reset_index().timestamp.astype(int) / 10**9
    timestamps.index = df.index
    timestamps = timestamps - timestamps.rolling(window_size).min()

    return values, timestamps


# cpu jiffies processing
def cpu_samples_to_df(samples):
    """ Converts a collection of CpuSample to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.cpu,
                stat.socket,
                stat.user,
                stat.nice,
                stat.system,
                stat.idle,
                stat.iowait,
                stat.irq,
                stat.softirq,
                stat.steal,
                stat.guest,
                stat.guest_nice
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'cpu',
        'socket',
        'user',
        'nice',
        'system',
        'idle',
        'iowait',
        'irq',
        'softirq',
        'steal',
        'guest',
        'guest_nice'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


ACTIVE_JIFFIES = [
    'cpu',
    'user',
    'nice',
    'system',
    'irq',
    'softirq',
    'steal',
    'guest',
    'guest_nice',
]


def compute_cpu_jiffies(samples):
    """ Computes the cpu jiffy rate of each bucket """
    if not isinstance(samples, pd.DataFrame):
        samples = cpu_samples_to_df(samples)
    samples['jiffies'] = samples[ACTIVE_JIFFIES].sum(axis=1)
    samples.timestamp = bucket_timestamps(samples.timestamp)

    jiffies = samples.groupby(['timestamp', 'socket', 'cpu']
                              ).jiffies.min().unstack().unstack()
    jiffies, ts = max_rolling_difference(jiffies)
    jiffies = jiffies.stack().stack().reset_index()
    jiffies = jiffies.groupby(['timestamp', 'socket', 'cpu']).sum().unstack()
    jiffies = jiffies.div(ts, axis=0).stack()[0]
    jiffies.name = 'jiffies'

    return jiffies


# task jiffies processing
def task_samples_to_df(samples):
    """ Converts a collection of ProcessSamples to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.task_id,
                stat.name if stat.HasField('name') else '',
                stat.cpu,
                stat.user,
                stat.system
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'id',
        'thread_name',
        'cpu',
        'user',
        'system',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def compute_task_jiffies(samples):
    """ Computes the app jiffy rate of each bucket """
    if not isinstance(samples, pd.DataFrame):
        samples = task_samples_to_df(samples)
    samples['jiffies'] = samples.user + samples.system
    samples.timestamp = bucket_timestamps(samples.timestamp)

    cpus = samples.groupby(['timestamp', 'id']).cpu.max()
    jiffies, ts = max_rolling_difference(samples.groupby([
        'timestamp',
        'id'
    ]).jiffies.min().unstack())
    jiffies = jiffies.stack().to_frame()
    jiffies = jiffies.groupby([
        'timestamp',
        'id',
    ])[0].sum().unstack().div(ts, axis=0).stack().to_frame()
    jiffies['cpu'] = cpus
    jiffies = jiffies.reset_index().set_index(['timestamp', 'id', 'cpu'])[0]
    jiffies.name = 'jiffies'

    return jiffies


# nvml processing
def nvml_samples_to_df(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.index,
                reading.bus_id,
                reading.power_usage,
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'device_index',
        'bus_id',
        'power_usage',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def compute_nvml_power(samples):
    """ Computes the power of each 50ms bucket """
    if not isinstance(samples, pd.DataFrame):
        samples = nvml_samples_to_df(samples)
    samples.timestamp = bucket_timestamps(samples.timestamp)
    samples = samples.groupby(
        ['timestamp', 'device_index', 'bus_id']).power_usage.min()
    samples.name = 'power'
    return samples


# rapl processing
WRAP_AROUND_VALUE = 16384


def rapl_samples_to_df(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.socket,
                reading.cpu,
                reading.package,
                reading.dram,
                reading.gpu
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'socket',
        'cpu',
        'package',
        'dram',
        'gpu'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def maybe_apply_wrap_around(value):
    """ Checks if the value needs to be adjusted by the wrap around. """
    if value < 0:
        return value + WRAP_AROUND_VALUE
    else:
        return value


def compute_rapl_power(samples):
    """ Computes the power of each 50ms bucket """
    if not isinstance(samples, pd.DataFrame):
        samples = rapl_samples_to_df(samples)
    samples.timestamp = bucket_timestamps(samples.timestamp)
    samples = samples.groupby(['timestamp', 'socket']).min()
    samples.columns.name = 'component'

    energy, ts = max_rolling_difference(samples.unstack())
    energy = energy.stack().stack().apply(maybe_apply_wrap_around)
    energy = energy.groupby([
        'timestamp',
        'socket',
        'component'
    ]).sum()
    power = energy.div(ts, axis=0).dropna()
    power.name = 'power'

    return power


# accounting
def account_with_activity(activity, power):
    """ Computes the product of the data across shared indices. """
    try:
        df = activity * power
        df.name = 'power'
        return df
    except Exception as e:
        # TODO: sometimes the data can't be aligned and i don't know why
        idx = list(set(activity.index.names) & set(power.index.names))
        print('data could not be directly aligned: {}'.format(e))
        print('forcing merge on {} instead'.format(idx))
        power = pd.merge(
            activity.reset_index(),
            power.reset_index(),
            on=['timestamp']
        ).set_index(idx)
        power = power.activity * power.power
        power.name = 'power'
        return power


def account_jiffies(samples, cpu=None):
    """ Returns the ratio of the jiffies with attribution corrections. """
    if not isinstance(samples, pd.DataFrame):
        tasks = compute_task_jiffies(samples.process)
        cpu = compute_cpu_jiffies(samples.cpu)
    else:
        tasks = samples

    activity = (tasks / cpu).dropna().replace(np.inf, 1).clip(0, 1)
    activity = activity[activity > 0]
    activity /= activity.groupby(['timestamp', 'cpu']).sum()
    activity.name = 'activity'
    return activity


def account_nvml(nvml):
    """ Returns the bucketed nvml power. """
    power = compute_nvml_power(nvml)
    power = power[power > 0].dropna() / 1000
    return power


def account_rapl(activity, rapl):
    """ Returns the product of activity and rapl power by socket. """
    power = compute_rapl_power(rapl)

    power = account_with_activity(activity, power)
    power = power[power > 0].dropna() / 1000000

    return power


def compute_footprint(data):
    """ Produces energy virtualizations from a data set. """
    activity = account_jiffies(data)

    footprints = {}
    footprints['activity'] = activity
    if len(data.nvml) > 0:
        footprints['nvml'] = account_nvml(data.nvml)
    if len(data.rapl) > 0:
        footprints['rapl'] = account_rapl(activity, data.rapl)

    return footprints
