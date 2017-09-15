LOCAL_PATH := $(call my-dir)

#Build .so  
#include $(CLEAR_VARS)
#LOCAL_MODULE := libmsc
#LOCAL_SRC_FILES_32 := libs/armeabi/libmsc.so
#LOCAL_MULTILIB := 32
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libavcodec
#LOCAL_SRC_FILES_32 := libs/armeabi/libavcodec.so
#LOCAL_MULTILIB := 32
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libeasemob_jni
#LOCAL_SRC_FILES_32 := libs/armeabi/libeasemob_jni.so
#LOCAL_MULTILIB := 32
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libeasemobservice
#LOCAL_SRC_FILES_32 := libs/armeabi/libeasemobservice.so
#LOCAL_MULTILIB := 32
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libjniavcodec
#LOCAL_SRC_FILES_32 := libs/armeabi/libjniavcodec.so
#LOCAL_MULTILIB := 32
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

#-----------------------------------------------------
#Build APK
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_PACKAGE_NAME := YYDRobotVoiceCamera
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets

#LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += \
	android-support-v4 \
	libcamera1 \
	libcamera2 

LOCAL_JNI_SHARED_LIBRARIES += libmsc #libavcodec libeasemob_jni libeasemobservice libjniavcodec
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libcamera1:libs/Msc_z.jar \
	libcamera2:libs/MotorService.jar 
	
include $(BUILD_MULTI_PREBUILT)	