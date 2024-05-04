#include <jni.h>

#ifndef _Included_jcarbon_server_MonotonicTimestamp
#define _Included_jcarbon_server_MonotonicTimestamp
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_jcarbon_server_MonotonicTimestamp_getMonotonicTimestamp
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif