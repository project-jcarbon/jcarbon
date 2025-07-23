#ifndef TIME_H
#define TIME_H

unsigned long usec_since_epoch();

unsigned long usec_monotonic_time();

int sleep_millisecond(long msec);

#endif //TIME_H
