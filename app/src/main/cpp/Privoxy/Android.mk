LOCAL_PATH := $(call my-dir)
#for android api level > 12
include $(CLEAR_VARS)
LOCAL_MODULE    := Privoxy
LOCAL_CFLAGS := -DANDROID_NDK  -DDEBUG_MODE -DBUILD_LIB
LOCAL_C_INCLUDES := $(LOCAL_PATH)/pcre/
LOCAL_SRC_FILES :=actions.c cgi.c cgiedit.c cgisimple.c deanimate.c encode.c filters.c gateway.c jbsockets.c jcc.c loadcfg.c loaders.c miscutil.c parsers.c pcrs.c list.c radix.c ssl.c ssl_common.c ssplit.c urlmatch.c
LOCAL_LDLIBS	+= -llog
include $(BUILD_SHARED_LIBRARY)

