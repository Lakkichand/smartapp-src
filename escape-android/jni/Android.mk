LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := proxy-info
LOCAL_SRC_FILES := proxy-info.cpp

include $(BUILD_SHARED_LIBRARY)
