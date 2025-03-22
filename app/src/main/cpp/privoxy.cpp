#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "PrivoxyNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
    // Include Privoxy headers here
    #include "Privoxy/jcc.h"
}

// Global variables for Privoxy configuration
static struct configuration_spec *config = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_supervpn_privoxy_PrivoxyManager_initPrivoxy(
        JNIEnv *env,
        jobject /* this */,
        jstring configPath) {
    
    const char *config_file = env->GetStringUTFChars(configPath, nullptr);
    
    try {


        // Load configuration
        if (shadowpath_main(config_file, config) < 0) {
            LOGE("Failed to load configuration from: %s", config_file);
            delete config;
            config = nullptr;
            return JNI_FALSE;
        }

        LOGI("Privoxy initialized successfully");
        return JNI_TRUE;
    } catch (...) {
        LOGE("Exception during Privoxy initialization");
        if (config) {
            delete config;
            config = nullptr;
        }
        return JNI_FALSE;
    } finally {
        env->ReleaseStringUTFChars(configPath, config_file);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_supervpn_privoxy_PrivoxyManager_stopPrivoxy(
        JNIEnv *env,
        jobject /* this */) {
    
    if (config) {
        // Cleanup Privoxy resources
        unload_config(config);
        delete config;
        config = nullptr;
        LOGI("Privoxy stopped successfully");
    }
}