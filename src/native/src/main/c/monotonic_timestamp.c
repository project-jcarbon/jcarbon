#include <jni.h>
#include <stdlib.h>
#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_jcarbon_server_MonotonicTimestamp_getMonotonicTimestamp
  (JNIEnv *env, jclass cls) {
	  struct timespec ts;
	  clock_gettime(CLOCK_MONOTONIC,&ts);
	  long long total_time = (ts.tv_sec*1000000000) + ts.tv_nsec;
	  return total_time;
  }

#ifdef __cplusplus
}
#endif