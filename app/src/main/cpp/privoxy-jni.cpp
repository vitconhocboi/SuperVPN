extern "C" int privoxy_main(int argc, char *argv[]);
extern "C" int stop_privoxy();

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


void signal_handler(int sig) {
    if (sig == SIGUSR1) {
        // Clean up any resources
        pthread_exit(NULL);  // Exit the thread
    }
}

// Thread function to run Privoxy
void *run_privoxy(void *arg) {
    char *config = (char *) arg;
    struct sigaction sa;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sa.sa_handler = signal_handler;
    sigaction(SIGUSR1, &sa, NULL);

    const char *argv[] = {
            "privoxy",
            "--no-daemon",
            config
    };

    LOGI("Starting Privoxy with config: %s", config);
    is_running = true;
    int result = privoxy_main(3, (char **) argv);
    is_running = false;
    LOGI("Privoxy exited with code: %d", result);

    return NULL;
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_akmobile_supervpn_network_PrivoxyManager_nativeStartPrivoxy(JNIEnv *env, jclass clazz,
                                                                     jstring config_path_java) {
    // If already running, return true
    if (is_running) {
        return JNI_TRUE;
    }

    // Convert Java string to C++ string
    const char *config_path_c = env->GetStringUTFChars(config_path_java, 0);
    config_path = std::string(config_path_c);
    env->ReleaseStringUTFChars(config_path_java, config_path_c);

    // Create thread to run Privoxy
    int result = pthread_create(&privoxy_thread, NULL, run_privoxy, (void *) config_path.c_str());

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

    stop_privoxy();
    pthread_exit(NULL);
    privoxy_thread = 0;
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_akmobile_supervpn_network_PrivoxyManager_nativeIsRunning(JNIEnv *env, jclass clazz) {
    return is_running ? JNI_TRUE : JNI_FALSE;
} 