package com.tici.vpn.proxy.master.gdpr

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import timber.log.Timber

class GoogleMobileAdsConsentManager private constructor(context: Context) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
    private val cmpUtils: CMPUtils = CMPUtils(context)

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean get() = consentInformation.canRequestAds()

    /** Helper variable to determine if the privacy options form is required. */
    private val isPrivacyOptionsRequired: Boolean get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun reset() {
        consentInformation.reset()
    }

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(
        activity: Activity,
        onCanShowAds: (() -> Unit),
        onDisableAds: (() -> Unit),
        timeout: Long = 1500
    ) {
//        /*Debug*/
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("FF2B80EE1C5DCA7A146F5303252D9DB3")
            .build()

        /*Release*/
        val params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).setTagForUnderAgeOfConsent(false).build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (isPrivacyOptionsRequired) {
                    if (cmpUtils.requiredShowCMPDialog()) {
                        UserMessagingPlatform.loadConsentForm(
                            activity,
                            { consentForm ->
                                val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
                                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                                    consentForm.show(activity) {
                                        // Callback when the form is dismissed
                                        val updatedStatus = consentInformation.consentStatus
                                        if (updatedStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                                            Timber.d("Consent User accepted consent")
                                            onCanShowAds()
                                        } else {
                                            Timber.d("Consent User rejected or skipped consent")
                                            onDisableAds()
                                        }
                                    }
                                } else {
                                    Timber.d("Consent Consent not required")
                                }
                            },
                            { formError ->
                                Timber.e("Consent Form load error: ${formError.message}")
                            }
                        )
                    } else {
                        onCanShowAds.invoke()
                    }
                } else {
                    onCanShowAds.invoke()
                }
            }, {
                if (canRequestAds) {
                    onCanShowAds.invoke()
                } else {
                    onDisableAds.invoke()
                }
            }
        )
    }

    /** Helper method to call the UMP SDK method to show the privacy options form. */
    fun showPrivacyOptionsForm(activity: Activity, onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
                }
    }
}