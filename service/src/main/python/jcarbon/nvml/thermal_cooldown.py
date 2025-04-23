from argparse import ArgumentParser
from collections import deque
from time import sleep, time
    
from pynvml import nvmlInit, nvmlDeviceGetCount
from pynvml import nvmlDeviceGetHandleByIndex, nvmlDeviceGetTemperature, NVML_TEMPERATURE_GPU

def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        '-p',
        '--period',
        dest='period',
        type=float,
        default=10,
        help='period to sample from the nvml',
    )
    parser.add_argument(
        '-t',
        '--temperature',
        dest='temperature',
        type=float,
        default=45,
        help='the target temperature in celsius',
    )
    return parser.parse_args()

def cooldown(period, temperature):
    k = 10
    p = 0.80 * k

    nvmlInit()
    gpu_count = nvmlDeviceGetCount()

    samples = {i: [] for i in range(gpu_count)}

    start = time()
    while(True):
        temps = {}
        met = {}
        sample_count = 0
        for i in range(gpu_count):
            handle = nvmlDeviceGetHandleByIndex(i)
            temp = nvmlDeviceGetTemperature(handle, NVML_TEMPERATURE_GPU)

            s = samples[i]
            s.append(temp)
            if(len(s) > k):
                s.pop(0)
            
            sample_count = len(s)

            temps[i] = sum(s) / len(s)
            met[i] = len(s) == k and sum(temp < temperature for temp in s) >= p

        zone_status = [f"{i}->{temps[i]} C ({met[i]})" for i in range(gpu_count)]
        status = ",".join(zone_status)

        message = f"zone status ({sample_count}/{k}): {status}"
        print(f"{message}{' ' * 50}", end="", flush=True)
        print("\b" * (len(message) + 50), end="", flush=True)
        if all(met.values()):
            break
        sleep(period)

    elapsed = time() - start
    print(f"cooled down to {temperature} C in {elapsed}{' ' * 50}")

def main():
    args = parse_args()
    cooldown(args.period, args.temperature)

if __name__ == '__main__':
    main()