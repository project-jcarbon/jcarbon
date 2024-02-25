#include <stdio.h>
#include <jni.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <math.h>
#include <stdint.h>
#include <string.h>
#include <inttypes.h>
#include <sys/types.h>

#include "energy_check_utils.h"
#include "arch_spec.h"
#include "msr.h"

// legacy jrapl flavour
JNIEXPORT jstring JNICALL
Java_jrapl_Rapl_readNative(JNIEnv *env, jclass jcls) {
	char energy_str[512];
	energy_stat_t energy_stat_per_socket[getSocketNum()];
	EnergyStatCheck(energy_stat_per_socket);
	energy_stat_csv_string(energy_stat_per_socket, energy_str);
	return (*env)->NewStringUTF(env, energy_str);
}

JNIEXPORT jint JNICALL
Java_jrapl_Rapl_wrapAround(JNIEnv * env, jclass jcls) {
  int fd = open("/dev/cpu/0/msr",O_RDONLY);
  double wraparound_energy = get_wraparound_energy(get_rapl_unit(fd).energy);
  close(fd);
  return wraparound_energy;
}

JNIEXPORT jint JNICALL
Java_jrapl_Rapl_dramWrapAround(JNIEnv * env, jclass jcls) {
  int fd = open("/dev/cpu/0/msr",O_RDONLY);
  int microarch = get_micro_architecture();
  double wraparound_energy = get_wraparound_energy (
    (microarch == BROADWELL || microarch == BROADWELL2)
      ? BROADWELL_MSR_DRAM_ENERGY_UNIT
      : get_rapl_unit(fd).energy
  );
  close(fd);
  return wraparound_energy;
}

JNIEXPORT jstring JNICALL
Java_jrapl_Rapl_components(JNIEnv* env, jclass jcls) {
	char* order;
	switch(get_power_domains_supported(get_micro_architecture())) {
		case DRAM_GPU_CORE_PKG:
			order = "dram,gpu,core,pkg";
			break;
		case DRAM_CORE_PKG:
			order = "dram,core,pkg";
			break;
		case GPU_CORE_PKG:
			order = "gpu,core,pkg";
			break;
		default:
			order = "undefined";
	}
	return (*env)->NewStringUTF(env, order);
}

// jcarbon flavour
JNIEXPORT jstring JNICALL
Java_jcarbon_cpu_rapl_Rapl_readNative(JNIEnv *env, jclass jcls) {
	char energy_str[512];
	energy_stat_t energy_stat_per_socket[getSocketNum()];
	EnergyStatCheck(energy_stat_per_socket);
	energy_stat_csv_string(energy_stat_per_socket, energy_str);
	return (*env)->NewStringUTF(env, energy_str);
}

JNIEXPORT jdouble JNICALL
Java_jcarbon_cpu_rapl_Rapl_wrapAround(JNIEnv * env, jclass jcls) {
  int fd = open("/dev/cpu/0/msr",O_RDONLY);
  double wraparound_energy = get_wraparound_energy(get_rapl_unit(fd).energy);
  close(fd);
  return wraparound_energy;
}

JNIEXPORT jdouble JNICALL
Java_jcarbon_cpu_rapl_Rapl_dramWrapAround(JNIEnv * env, jclass jcls) {
  int fd = open("/dev/cpu/0/msr",O_RDONLY);
  int microarch = get_micro_architecture();
  double wraparound_energy = get_wraparound_energy (
    (microarch == BROADWELL || microarch == BROADWELL2)
      ? BROADWELL_MSR_DRAM_ENERGY_UNIT
      : get_rapl_unit(fd).energy
  );
  close(fd);
  return wraparound_energy;
}

JNIEXPORT jstring JNICALL
Java_jcarbon_cpu_rapl_Rapl_components(JNIEnv* env, jclass jcls) {
	char* order;
	switch(get_power_domains_supported(get_micro_architecture())) {
		case DRAM_GPU_CORE_PKG:
			order = "dram,gpu,core,pkg";
			break;
		case DRAM_CORE_PKG:
			order = "dram,core,pkg";
			break;
		case GPU_CORE_PKG:
			order = "gpu,core,pkg";
			break;
		default:
			order = "undefined";
	}
	return (*env)->NewStringUTF(env, order);
}