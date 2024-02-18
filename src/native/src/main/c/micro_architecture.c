#include <jni.h>

#include "arch_spec.h"

JNIEXPORT jstring JNICALL
Java_jrapl_MicroArchitecture_name(JNIEnv * env, jclass jcls) {
  const char* name;
  switch(get_micro_architecture()) {
    case KABYLAKE:			  name = "KABYLAKE";			  break;
    case BROADWELL:			  name = "BROADWELL";			  break;
    case SANDYBRIDGE_EP:	name = "SANDYBRIDGE_EP";	break;
    case HASWELL3:			  name = "HASWELL3";			  break;
    case SKYLAKE2:			  name = "SKYLAKE2";			  break;
    case APOLLOLAKE:		  name = "APOLLOLAKE";		  break;
    case SANDYBRIDGE:		  name = "SANDYBRIDGE";		  break;
    case IVYBRIDGE:			  name = "IVYBRIDGE";			  break;
    case HASWELL1:			  name = "HASWELL1";			  break;
    case HASWELL_EP:		  name = "HASWELL_EP";		  break;
    case COFFEELAKE2:		  name = "COFFEELAKE2";		  break;
    case BROADWELL2:		  name = "BROADWELL2";		  break;
    case HASWELL2:			  name = "HASWELL2";			  break;
    case SKYLAKE1:			  name = "SKYLAKE1";			  break;
    default:              name = "UNDEFINED_MICROARCHITECTURE";
  }
  return (*env)->NewStringUTF(env, name);
}

JNIEXPORT jint JNICALL
Java_jrapl_MicroArchitecture_sockets(JNIEnv * env, jclass jcls) {
  return getSocketNum();
}
