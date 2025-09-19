package com.core.rate.feedback

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.core.rate.R
import java.util.Locale


object ShareUtils {

    private fun shareAppEmail(context: Context, subject: String, body: String) {
        try {
            //When Gmail App is not installed or disable
            val feedbackIntent = Intent(Intent.ACTION_SEND)
            feedbackIntent.type = "message/rfc822"
            feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            feedbackIntent.putExtra(Intent.EXTRA_TEXT, body)
            if (feedbackIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(feedbackIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val PACKAGE_MAIL_LIST = mutableListOf<String>().apply {
        add("com.android.bluetooth")
        add("com.samsung.android.app.sharelive")
        add("com.whatsapp")
        add("com.microsoft.skydrive")

    }

    private fun getAppName(context: Context): String {
        val labelResId = context.applicationInfo.labelRes
        return if (labelResId != 0) context.getString(labelResId) else "Unknown App Name"
    }

    fun getAppVersionName(context: Context): String {
        val packageManager = context.packageManager
        val packageName = context.packageName
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "Unknown Version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown Version"
        }
    }

    fun feedbackFocusEmail(context: Context, feedback: Feedback) {
        kotlin.runCatching {
            val email = context.getString(R.string.fb_email_feedback)
            val appName = getAppName(context)
            val versionName = getAppVersionName(context)
            val subject = String.format(
                "%s %s v%s",
                context.getString(R.string.fb_text_feedback_to),
                appName,
                getAppVersionName(context)
            )
            val tagBuilder = StringBuilder()
            if (feedback.isFeatureQuality) {
                tagBuilder.append("#Feature quality ")
            }
            if (feedback.isCrash) {
                tagBuilder.append("#Crash ")
            }
            if (feedback.isBug) {
                tagBuilder.append("#Bug ")
            }
            if (feedback.isOthers) {
                tagBuilder.append("#Others ")
            }

            val info = getDeviceAndAppMemoryInfo(context)

            val builder =
                StringBuilder(tagBuilder.toString())
                    .append("\n\n")
                    .append(feedback.content).append("\n\n\n\n")
                    .append(appName).append(" v").append(versionName).append("\n\n")
                    .append("Device Model: ").append(getDeviceName()).append("\n")
                    .append("Android Version: ").append(Build.VERSION.RELEASE).append("\n")
                    .append("Resolution: ").append(getResolution()).append("\n")
                    .append("Total Storage: ").append(info["Total Storage"]).append("\n")
                    .append("Free Storage: ").append(info["Free Storage"]).append("\n")
                    .append("Total RAM: ").append(info["Total RAM"]).append("\n")
                    .append("Free RAM: ").append(info["Free RAM"]).append("\n")
                    .append("Low Memory: ").append(info["Low Memory"]).append("\n")

            val body = builder.toString()
            val feedbackIntent = Intent(Intent.ACTION_SEND)
            feedbackIntent.type = "message/rfc822"
            feedbackIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            feedbackIntent.putExtra(Intent.EXTRA_TEXT, body)
            val queryIntentActivities = context.packageManager.queryIntentActivities(
                Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("mailto:")
                ), PackageManager.MATCH_DEFAULT_ONLY
            )

            val list = ArrayList<Intent>()
            if (queryIntentActivities.isNotEmpty()) {
                queryIntentActivities.forEach {
                    val activityInfo = it.activityInfo
                    list.add(Intent(feedbackIntent).apply {
                        setPackage(activityInfo.packageName)
                        setComponent(ComponentName(activityInfo.packageName, activityInfo.name))
                    })
                }
            } else {
                list.add(feedbackIntent)
            }

            val chooserIntent =
                Intent.createChooser(list.removeAt(0), context.getString(R.string.fb_share_with))
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, list.toTypedArray())
            context.startActivity(chooserIntent)
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun feedbackNew(context: Context, feedback: Feedback) {
        val email = context.getString(R.string.fb_email_feedback)
        val appName = getAppName(context)
        val versionName = getAppVersionName(context)
        val subject = String.format(
            "%s %s v%s",
            context.getString(R.string.fb_text_feedback_to),
            appName,
            getAppVersionName(context)
        )
        val tagBuilder = StringBuilder()
        if (feedback.isFeatureQuality) {
            tagBuilder.append("#Feature quality ")
        }
        if (feedback.isCrash) {
            tagBuilder.append("#Crash ")
        }
        if (feedback.isBug) {
            tagBuilder.append("#Bug ")
        }
        if (feedback.isOthers) {
            tagBuilder.append("#Others ")
        }

        val info = getDeviceAndAppMemoryInfo(context)

        val builder =
            StringBuilder(tagBuilder.toString())
                .append("\n\n")
                .append(feedback.content).append("\n\n\n\n")
                .append(appName).append(" v").append(versionName).append("\n\n")
                .append("Device Model: ").append(getDeviceName()).append("\n")
                .append("Android Version: ").append(Build.VERSION.RELEASE).append("\n")
                .append("Resolution: ").append(getResolution()).append("\n")
                .append("Total Storage: ").append(info["Total Storage"]).append("\n")
                .append("Free Storage: ").append(info["Free Storage"]).append("\n")
                .append("Total RAM: ").append(info["Total RAM"]).append("\n")
                .append("Free RAM: ").append(info["Free RAM"]).append("\n")
                .append("Low Memory: ").append(info["Low Memory"]).append("\n")

        val body = builder.toString()
        val feedbackIntent = Intent(Intent.ACTION_SEND)
        feedbackIntent.type = "message/rfc822"
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        feedbackIntent.putExtra(Intent.EXTRA_TEXT, body)

        val chooserIntent = getIntentChooser(
            context,
            feedbackIntent,
            context.getString(R.string.fb_share_with),
            object : ComponentNameFilter {
                override fun shouldBeFilteredOut(componentName: ComponentName): Boolean {
                    return PACKAGE_MAIL_LIST.contains(componentName.packageName)
                }
            })
        context.startActivity(chooserIntent)
    }

    fun feedback(context: Context, feedback: Feedback, appName: String) {
        val email = context.getString(R.string.fb_email_feedback)
        val pack = "com.google.android.gm"
        val versionName = getAppVersionName(context)
        val subject = String.format(
            "%s %s v%s",
            context.getString(R.string.fb_text_feedback_to),
            appName,
            versionName
        )
        val tagBuilder = StringBuilder()
        if (feedback.isFeatureQuality) {
            tagBuilder.append("# Feature quality ")
        }
        if (feedback.isCrash) {
            tagBuilder.append("# Crash ")
        }
        if (feedback.isBug) {
            tagBuilder.append("# Bug ")
        }
        if (feedback.isOthers) {
            tagBuilder.append("# Others ")
        }

        val info = getDeviceAndAppMemoryInfo(context)

        val builder =
            StringBuilder(tagBuilder.toString())
                .append("\n\n")
                .append(feedback.content).append("\n\n\n\n")
                .append(appName).append(" v").append(versionName).append("\n\n")
//                .append("Package Name: ").append(BuildConfig.APPLICATION_ID).append("\n")
                .append("Device Model: ").append(getDeviceName()).append("\n")
                .append("Android Version: ").append(Build.VERSION.RELEASE).append("\n")
                .append("Resolution: ").append(getResolution()).append("\n")
                .append("Total Storage: ").append(info["Total Storage"]).append("\n")
                .append("Free Storage: ").append(info["Free Storage"]).append("\n")
                .append("Total RAM: ").append(info["Total RAM"]).append("\n")
                .append("Free RAM: ").append(info["Free RAM"]).append("\n")
                .append("Low Memory: ").append(info["Low Memory"]).append("\n")
        val body = builder.toString()
        if (context.packageManager.getLaunchIntentForPackage(pack) != null) {
            try {
                val feedbackIntent = Intent(Intent.ACTION_SEND)
                feedbackIntent.type = "message/rfc822"
                feedbackIntent.setPackage(pack)
                feedbackIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                feedbackIntent.putExtra(Intent.EXTRA_TEXT, body)
                context.startActivity(feedbackIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                feedback2(context, subject, body)
            }
        } else {
            feedback2(context, subject, body)
        }
    }

    private fun getStorageInfo(): Pair<Long, Long> {
        val path = Environment.getDataDirectory() // Bộ nhớ trong (Internal Storage)
        val stat = StatFs(path.path)

        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalStorage = totalBlocks * blockSize
        val freeStorage = availableBlocks * blockSize

        return Pair(totalStorage, freeStorage) // Trả về total và free space (đơn vị: byte)
    }

    private fun getDeviceAndAppMemoryInfo(context: Context): Map<String, String> {
        val (totalStorage, freeStorage) = getStorageInfo()
        val memoryInfo = getAppMemoryInfo(context)

        return mapOf(
            "Total Storage" to "${formatSize(totalStorage)} GB",
            "Free Storage" to "${formatSize(freeStorage)} GB",
            "Total RAM" to "${formatSize(memoryInfo.totalMem)} GB",
            "Free RAM" to "${formatSize(memoryInfo.availMem)} GB",
            "Low Memory" to "${memoryInfo.lowMemory}"
        )
    }

    private fun formatSize(sizeInBytes: Long): String {
        return String.format(
            Locale.US,
            "%.1f",
            sizeInBytes / 1_073_741_824.0
        ) // Định dạng đến 1 chữ số thập phân
    }

    private fun getAppMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    private fun feedback2(context: Context, subject: String, body: String) {
        try {
            val email = context.getString(R.string.fb_email_feedback)
            //When Gmail App is not installed or disable
            val feedbackIntent = Intent(Intent.ACTION_SEND)
            feedbackIntent.type = "message/rfc822"
            feedbackIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            feedbackIntent.putExtra(Intent.EXTRA_TEXT, body)
            if (feedbackIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(feedbackIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }
        } else {
            manufacturer.lowercase(Locale.getDefault())
                .replaceFirstChar { it.titlecase() } + " " + model
        }
    }

    private fun getResolution(): String {
        val builder = StringBuilder()
        builder.append(Resources.getSystem().displayMetrics.heightPixels).append("x")
            .append(Resources.getSystem().displayMetrics.widthPixels)
        return builder.toString()
    }

    private fun getIntentChooser(
        context: Context,
        intent: Intent,
        chooserTitle: CharSequence? = null,
        filter: ComponentNameFilter,
    ): Intent? {
        val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
        val excludedComponentNames = HashSet<ComponentName>()
        resolveInfo.forEach {
            val activityInfo = it.activityInfo
            val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
            if (filter.shouldBeFilteredOut(componentName)) {
                excludedComponentNames.add(componentName)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Intent.createChooser(intent, chooserTitle)
                .putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames.toTypedArray())
        }
        if (resolveInfo.isNotEmpty()) {
            val targetIntents: MutableList<Intent> = ArrayList()
            for (resolve in resolveInfo) {
                val activityInfo = resolve.activityInfo
                if (excludedComponentNames.contains(
                        ComponentName(
                            activityInfo.packageName,
                            activityInfo.name
                        )
                    )
                )
                    continue
                val targetIntent = Intent(intent)
                targetIntent.setPackage(activityInfo.packageName)
                targetIntent.component = ComponentName(activityInfo.packageName, activityInfo.name)
                // wrap with LabeledIntent to show correct name and icon
                val labeledIntent = LabeledIntent(
                    targetIntent,
                    activityInfo.packageName,
                    resolve.labelRes,
                    resolve.icon
                )
                // add filtered intent to a list
                targetIntents.add(labeledIntent)
            }
            // deal with M list separate problem
            val chooserIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // create chooser with empty intent in M could fix the empty cells problem
                Intent.createChooser(Intent(), chooserTitle)
            } else {
                // create chooser with one target intent below M
                Intent.createChooser(targetIntents.removeAt(0), chooserTitle)
            }
            if (chooserIntent == null) {
                return null
            }
            // add initial intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
            return chooserIntent
        }
        return null
    }

    interface ComponentNameFilter {
        fun shouldBeFilteredOut(componentName: ComponentName): Boolean
    }
}