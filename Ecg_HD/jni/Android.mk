# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := hello-jni
LOCAL_SRC_FILES := heart_rate_detect.cpp \
                   hello-jni.cpp \
                   1_lpfilter.cpp \
                   pinghua.cpp \
                   wing_function.cpp \
                   array_operate.cpp \
                   ecg_detect.cpp \
                   filter.cpp \
                   keypoints_detect.cpp \
                   knee_detect.cpp \
                   lpfilter.cpp \
                   moving_detect.cpp \
                   my_filter.cpp \
                   P_detect.cpp \
                   para_judgement.cpp \
                   positive_peak_detect.cpp \
                   QS_detect.cpp \
                   R_detect.cpp \
                   T_detect.cpp \
                   parseEcgInfo.cpp

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)