#include <jni.h>

#include "time.h"

/** Returns the unixtime as microseconds. */
JNIEXPORT jlong JNICALL Java_jcarbon_util_Timestamps_epochTimeNative
  (JNIEnv *env, jclass jcls) {
	return usec_since_epoch();
}

JNIEXPORT jlong JNICALL Java_jcarbon_util_Timestamps_monotonicTimeNative
  (JNIEnv *env, jclass jcls) {
	return usec_monotonic_time();
}
