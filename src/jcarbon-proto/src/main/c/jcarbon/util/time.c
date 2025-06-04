#include <time.h>
#include <errno.h>
#include <sys/time.h>

unsigned long
usec_since_epoch() {
	struct timeval t; gettimeofday(&t, 0);
	return t.tv_sec * 1000000UL + t.tv_usec;
}

unsigned long
usec_monotonic_time() {
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	long long total_time = (ts.tv_sec * 1000000000) + ts.tv_nsec;
	return total_time;
}
