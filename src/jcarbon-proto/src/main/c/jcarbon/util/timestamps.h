#include <jni.h>

#ifndef _Included_jcarbon_util_Timestamps
#define _Included_jcarbon_util_Timestamps

JNIEXPORT jlong JNICALL Java_jcarbon_util_Timestamps_epochTimeNative
   (JNIEnv *, jclass);

JNIEXPORT jlong JNICALL Java_jcarbon_util_Timestamps_monotonicTimeNative
   (JNIEnv *, jclass);
