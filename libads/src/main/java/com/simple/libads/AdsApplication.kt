package com.simple.libads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.simple.libads.base.OpenAdLoader
import com.simple.libads.config.OpenConfig
import com.simple.libads.manager.OpenApplicationManager
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber


abstract class AdsApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    private lateinit var mOpenApplicationManager: OpenApplicationManager
    var currentActivity: Activity? = null

    companion object {
        lateinit var appContext: Context
        var isShowInterAds = MutableStateFlow<Boolean>(false)
        var dialogLoadingBuilder = DialogLoadingBuilder()
        var adsEnable : Boolean = true
    }

    abstract fun isInitAds(): Boolean

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        appContext = this
        mOpenApplicationManager = OpenApplicationManager()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun setupAds(openConfig: OpenConfig) {
        mOpenApplicationManager.setup(context = this, config = openConfig)
    }

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        currentActivity?.let {
            if(!isShowInterAds.value && isInitAds()) {
                mOpenApplicationManager.showAdIfAvailable(it, object : OpenAdLoader.OnShowAdCompleteListener {
                    override fun onShowAdComplete(isShow: Boolean) {
                    }
                })
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    /**
     * Shows an app open ad.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OpenAdLoader.OnShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        if(!isShowInterAds.value) {
            mOpenApplicationManager.showAdIfAvailable(activity, onShowAdCompleteListener)
        }
    }


    /**
     * Load an app open ad.
     *
     * @param activity the activity that shows the app open ad
     */
    fun loadAd(activity: Activity) {
        // We wrap the loadAd to enforce that other classes only interact with MyApplication
        // class.
        mOpenApplicationManager.loadAd(activity)
    }

    /** Inner class that loads and shows app open ads. */
}