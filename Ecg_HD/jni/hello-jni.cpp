/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include "heart_rate_detect.h"
#include "head_ecg.h"
#include <stdio.h>
#include <stdlib.h>

#include <android/log.h>
#define TAG "hello"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
 */

extern "C" {
    JNIEXPORT jstring JNICALL Java_com_nju_ecg_service_EcgService_stringFromJNI( JNIEnv* env, jobject thiz );
    JNIEXPORT jint JNICALL Java_com_nju_ecg_service_EcgService_sumArray( JNIEnv* env, jobject thiz, jintArray arr);
	JNIEXPORT jintArray JNICALL Java_com_nju_ecg_service_EcgService_setEcgData(JNIEnv *env, jobject thiz, jintArray arr);
	JNIEXPORT jintArray JNICALL Java_com_nju_ecg_service_EcgService_ecgFilter(JNIEnv *env, jobject thiz, jintArray arr, int length);
	JNIEXPORT jintArray JNICALL Java_com_nju_ecg_service_EcgService_getEcgParameter(JNIEnv *env, jobject thiz, jintArray arr);
    JNIEXPORT jobjectArray JNICALL Java_com_nju_ecg_service_EcgService_initInt2DArray(JNIEnv* env, jclass cls, int size);
};

JNIEXPORT jstring JNICALL
Java_com_nju_ecg_service_EcgService_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return env->NewStringUTF("Hello from JNI, hello world,hi !");
}

JNIEXPORT jint JNICALL
Java_com_nju_ecg_service_EcgService_sumArray( JNIEnv* env, jobject thiz, jintArray arr)
{
	jint i, sum = 0;
	jint buf[10];
	env ->GetIntArrayRegion(arr, 0, 10, buf);
	for(i = 0; i < 10; i++) {
		sum += buf[i];
	}
	return sum;
}

int ECG_DATA_LENGTH = 1000 * 10;
int getHeartData(double *buf, int buf_len);
void writeEcgDataToFile(int *buf, int len);
JNIEXPORT jintArray JNICALL
Java_com_nju_ecg_service_EcgService_setEcgData(JNIEnv *env, jobject thiz, jintArray arr)
{
	jint buf[ECG_DATA_LENGTH];
	env->GetIntArrayRegion(arr, 0, ECG_DATA_LENGTH, buf);
	double dbuf[ECG_DATA_LENGTH];
	for(int i = 0; i < ECG_DATA_LENGTH; i++) {
		dbuf[i] = buf[i];
	    //LOGV("%d = %f ", i, dbuf[i]);
	}

	double afterFilter[ECG_DATA_LENGTH];
	my_filter(dbuf, afterFilter, ECG_DATA_LENGTH);
	for(int i = 0; i < ECG_DATA_LENGTH; i++) {
	    //LOGV("%d = %f ", i, afterFilter[i]);
	}
    
	//ecg_detect(afterFilter, ECG_DATA_LENGTH, 1000);
	//jint* para = get_ecg_para(afterFilter, ECG_DATA_LENGTH, 1000);
    //LOGV("ret length = %d ", para);
	jint* ret = heart_rate_detect(afterFilter+200, ECG_DATA_LENGTH-200, 1000); //modified by Huo
	LOGV("ret length = %d ", ret[0]);

	jintArray iarr = env->NewIntArray(ret[0] + 1);
    if (iarr == NULL) return NULL;
	 env->SetIntArrayRegion(iarr, 0, ret[0] + 1, ret);
	return iarr;
}

JNIEXPORT jintArray JNICALL
Java_com_nju_ecg_service_EcgService_ecgFilter(JNIEnv *env, jobject thiz, jintArray arr, int length)
{
	jint buf[length];
	env->GetIntArrayRegion(arr, 0, length, buf);
	double dbuf[length];
	for(int i = 0; i < length; i++) {
		dbuf[i] = buf[i];
	    //LOGV("%d = %f ", i, dbuf[i]);
	}

	double afterFilter[length];
	my_filter(dbuf, afterFilter, length);
    jint iAfterFilter[length];
	for(int i = 0; i < length; i++) {
		iAfterFilter[i] = (int) afterFilter[i];
	    //LOGV("%d = %f ", i, afterFilter[i]);
	}

	jintArray iarr = env->NewIntArray(length);
    if (iarr == NULL) return NULL;
	 env->SetIntArrayRegion(iarr, 0, length, iAfterFilter);
	return iarr;
}

JNIEXPORT jintArray JNICALL
Java_com_nju_ecg_service_EcgService_getEcgParameter(JNIEnv *env, jobject thiz, jintArray arr)
{
	jclass errCls; 
	env->ExceptionDescribe(); 
	env->ExceptionClear(); 
	errCls = env->FindClass("java/lang/Exception"); 
	try{
		jint buf[ECG_DATA_LENGTH];
		env->GetIntArrayRegion(arr, 0, ECG_DATA_LENGTH, buf);
		double dbuf[ECG_DATA_LENGTH];
		for(int i = 0; i < ECG_DATA_LENGTH; i++) {
			dbuf[i] = buf[i];
		    //LOGV("%d = %f ", i, dbuf[i]);
		}
	    double afterFilter[ECG_DATA_LENGTH];
		my_filter(dbuf, afterFilter, ECG_DATA_LENGTH);
	    //jint* ret = ecg_detect(afterFilter, ECG_DATA_LENGTH, 1000);
		jint * ret = get_ecg_para(afterFilter+200, ECG_DATA_LENGTH-200, 1000); //modified by Huo
	
		for(int i = 0; i < 15; i++) {
			LOGV("%d = %d ", i, ret[i]);
		}
		jintArray iarr = env->NewIntArray(101); // return 14+1 parameters
	    if (iarr == NULL) return NULL;
		 env->SetIntArrayRegion(iarr, 0, 101, ret);
		return iarr;
	}catch(jclass errCls){
		env->ThrowNew(errCls, "thrown from C++ code"); 
	}
}

int getHeartData(double *buf, int buf_len);

JNIEXPORT jobjectArray JNICALL Java_com_nju_ecg_service_EcgService_initInt2DArray(JNIEnv* env, jclass cls, int size) {
	jobjectArray result;
	jint i;
	jclass intArrCls = env-> FindClass("[I");
	if(intArrCls == NULL) return NULL;

	result = env->NewObjectArray(size, intArrCls, NULL);
	if(result == NULL) return NULL;

	for(i = 0; i < size; i++) {
		jint tmp[256];
		jint j;
		jintArray iarr = env->NewIntArray(size);
		if(iarr == NULL) return NULL;
		for(j = 0; j < size; j++) tmp[j] = i + j;
		env->SetIntArrayRegion(iarr, 0, size, tmp);
		env->SetObjectArrayElement(result, i, iarr);
		env->DeleteLocalRef(iarr);
	}

	return result;
}

#define HEART_FILE "/sdcard/hello.txt"
void writeEcgDataToFile(int *buf, int len) {
	FILE* file = fopen(HEART_FILE,"a+");

    if (file != NULL)
    {
		fwrite(buf, sizeof(int), len, file);
        fputs("HELLO WORLD2!\n", file);
        fflush(file);
        fclose(file);
    }
}

int getHeartData(double *buf, int buf_len) {
	
    FILE *fp = fopen(HEART_FILE, "rb");
    if (NULL == fp)
    {
        LOGV("fopen %s failed\n", HEART_FILE);
        return -1;
    }

	char* rawBuf = (char *)calloc(1024, sizeof(char));
	int nRead = 0;
	int count = 0;
	for(count = 0; count < 4096/(1024/8); count++) {
        nRead = fread(rawBuf, count*1024, 1024, fp);
		if(nRead < 1024) {	LOGV(" %d",nRead); break;}
	    for(int i = 0; i < nRead;) {
				LOGV(" raw buffer %d %d", rawBuf[5], rawBuf[6]);
		    buf[count] = rawBuf[i+5] << 16 |  rawBuf[i+6] << 8 | rawBuf[i+7]; 
		    i += 8;
		}
	}
    fclose(fp);

    if (count > 0)
    {
        return count;
    }

    LOGV("fread %s failed\n", HEART_FILE);
    return -1;
}

