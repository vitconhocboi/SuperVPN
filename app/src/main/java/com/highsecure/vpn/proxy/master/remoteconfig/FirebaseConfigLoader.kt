//package com.highsecure.vpn.proxy.master.remoteconfig
//
//import com.common.baseui.extension.Utils.postDelay
//import com.google.firebase.remoteconfig.ConfigUpdate
//import com.google.firebase.remoteconfig.ConfigUpdateListener
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//import kotlin.math.pow
//
//class FirebaseConfigLoader @Inject constructor() : ConfigLoader {
//    private var retryAttemptInter = 0.0
//    protected val MAX_RETRY = 5.0
//    override fun fetch(onComplete: (() -> Unit)) {
//        val remoteConfig = FirebaseRemoteConfig.getInstance()
//        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                retryAttemptInter = 0.0
//                FirebaseRemoteConfig.getInstance().activate()
//                process()
//                onComplete.invoke()
//            } else {
//                tryFetchAgain(onComplete)
//            }
//        }
//
//        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
//            override fun onUpdate(task: ConfigUpdate) {
//                FirebaseRemoteConfig.getInstance().activate()
//                process()
//            }
//
//            override fun onError(p0: FirebaseRemoteConfigException) {
//
//            }
//        })
//    }
//
//    private fun process() {
//        val adConfig = FirebaseRemoteConfig.getInstance().getString(
//            FirebaseConfigManager.KEY_AD_CONFIG
//        )
//        if (adConfig.isNotEmpty()) {
//            try {
//                FirebaseConfigManager.get().setConfig(adConfig, true)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        val pixelConfig = FirebaseRemoteConfig.getInstance().getString(
//            FirebaseConfigManager.KEY_APP_CONFIG
//        )
//        if (pixelConfig.isNotEmpty()) {
//            try {
//                FirebaseConfigManager.get().setAppConfig(pixelConfig, true)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//
//    private fun tryFetchAgain(onFetchComplete: (() -> Unit)) {
//        ++retryAttemptInter
//        if (retryAttemptInter < MAX_RETRY) {
//            val delayMillis =
//                TimeUnit.SECONDS.toMillis(
//                    2.0.pow(6.0.coerceAtMost(retryAttemptInter)).toLong()
//                )
//            postDelay(delayMillis) { fetch(onFetchComplete) }
//        }
//    }
//}