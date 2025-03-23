extern "C" int privoxy_main(int argc, char *argv[]);

#include <jni.h>
#include <string>
#include <pthread.h>
#include <android/log.h>
#include <jcc.h>
#include <unistd.h>

// Define the log tag
#define LOG_TAG "PrivoxyJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


// Global variables
static pthread_t privoxy_thread = 0;
static bool is_running = false;
static std::string config_path;

// Thread function to run Privoxy
void *run_privoxy(void *arg) {
    char *config = (char*)arg;
    
    const char *argv[] = {
        "privoxy",
        "--no-daemon",
        config
    };
    
    LOGI("Starting Privoxy with config: %s", config);
    is_running = true;
    int result = privoxy_main(3, (char**)argv);
    is_running = false;
    LOGI("Privoxy exited with code: %d", result);
    
    return NULL;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_akmobile_supervpn_network_PrivoxyManager_nativeStartPrivoxy(JNIEnv *env, jclass clazz, jstring config_path_java) {
    // If already running, return true
    if (is_running) {
        return JNI_TRUE;
    }
    
    // Convert Java string to C++ string
    const char *config_path_c = env->GetStringUTFChars(config_path_java, 0);
    config_path = std::string(config_path_c);
    env->ReleaseStringUTFChars(config_path_java, config_path_c);
    
    // Create thread to run Privoxy
    int result = pthread_create(&privoxy_thread, NULL, run_privoxy, (void*)config_path.c_str());
    
    if (result != 0) {
        LOGE("Failed to create Privoxy thread: %d", result);
        return JNI_FALSE;
    }
    
    // Sleep for 500ms to ensure Privoxy starts
    
    usleep(500000); // 500ms = 500,000 microseconds
    
    return is_running ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_akmobile_supervpn_network_PrivoxyManager_nativeStopPrivoxy(JNIEnv *env, jclass clazz) {
    if (!is_running) {
        return JNI_TRUE;
    }
    // Signal Privoxy to stop
    LOGI("Stopping Privoxy...");
    
    // Set running flag to false
    is_running = false;
    
    // Send a termination signal to the thread
    pthread_kill(privoxy_thread, SIGTERM);
    
    // Give it a moment to process the signal
    usleep(100000); // 100ms

    
// //    // Call Privoxy's stop function
// //    privoxy_stop();
    
//     // Wait for thread to finish (with timeout)
//     struct timespec ts;
//     clock_gettime(CLOCK_REALTIME, &ts);
//     ts.tv_sec += 5; // 5 second timeout
    
//     int result = pthread_timedjoin_np(privoxy_thread, NULL, &ts);
    
//    if (result != 0) {
//        LOGE("Failed to join Privoxy thread: %d", result);
//        // Force cancel thread as last resort
//        pthread_cancel(privoxy_thread);
//        is_running = false;
//        return JNI_FALSE;
//    }
    
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_akmobile_supervpn_network_PrivoxyManager_nativeIsRunning(JNIEnv *env, jclass clazz) {
    return is_running ? JNI_TRUE : JNI_FALSE;
} 