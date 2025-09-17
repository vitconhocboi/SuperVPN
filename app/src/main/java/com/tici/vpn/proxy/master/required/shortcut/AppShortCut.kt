package com.tici.vpn.proxy.master.required.shortcut

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.feature.feature_splash.ui.SplashActivity

object AppShortCut {
    const val KEY_SHORTCUT_TARGET_SCREEN = "KEY_SHORTCUT_TARGET_SCREEN"
    private const val shortcut_uninstall = "id_uninstall"
    private const val shortcut_screen_1 = "id_screen_1"
    private const val shortcut_screen_2 = "id_screen_2"

    fun setUpShortCut(context: Context, isEnable: Boolean, isEnableUninstall: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                val recoverOpenVPNIntent =
                    Intent(Intent.ACTION_VIEW, null, context, SplashActivity::class.java).apply {
                        val bundle = Bundle()
                        bundle.putString(
                            KEY_SHORTCUT_TARGET_SCREEN,
                            AppScreenType.Screen1.screenName
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        putExtras(bundle)
                    }

                val recoveryDiscoverIntent =
                    Intent(Intent.ACTION_VIEW, null, context, SplashActivity::class.java).apply {
                        val bundle = Bundle()
                        bundle.putString(
                            KEY_SHORTCUT_TARGET_SCREEN,
                            AppScreenType.Screen2.screenName
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        putExtras(bundle)
                    }


                val recoveryUninstallIntent =
                    Intent(Intent.ACTION_VIEW, null, context, SplashActivity::class.java).apply {
                        val bundle = Bundle()
                        bundle.putString(
                            KEY_SHORTCUT_TARGET_SCREEN,
                            AppScreenType.Uninstall.screenName
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        putExtras(bundle)
                    }

                val shortCutUninstall = ShortcutInfoCompat.Builder(context, shortcut_uninstall)
                    .setShortLabel(context.getString(R.string.core_shortcut_short_label_uninstall))
                    .setLongLabel(context.getString(R.string.core_shortcut_long_label_uninstall))
                    .setIcon(
                        IconCompat.createWithResource(
                            context,
                            R.drawable.ic_short_cut_uninstall
                        )
                    )
                    .setIntent(recoveryUninstallIntent)
                    .build()

                val shortCutScreen1 = ShortcutInfoCompat.Builder(context, shortcut_screen_1)
                    .setShortLabel(context.getString(R.string.shortcut_short_label_screen_1))
                    .setLongLabel(context.getString(R.string.shortcut_long_label_screen_1))
                    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_short_cut_vpn))
                    .setIntent(recoverOpenVPNIntent)
                    .build()

                val shortCutScreen2 = ShortcutInfoCompat.Builder(context, shortcut_screen_2)
                    .setShortLabel(context.getString(R.string.shortcut_short_label_screen_2))
                    .setLongLabel(context.getString(R.string.shortcut_long_label_screen_2))
                    .setIcon(
                        IconCompat.createWithResource(
                            context,
                            R.drawable.ic_short_cut_discover
                        )
                    )
                    .setIntent(recoveryDiscoverIntent)
                    .build()

                if (isEnable) {
                    ShortcutManagerCompat.removeAllDynamicShortcuts(context)
                    ShortcutManagerCompat.setDynamicShortcuts(
                        context, if (isEnableUninstall) {
                            listOf(shortCutScreen1, shortCutScreen2, shortCutUninstall)
                        } else {
                            listOf(shortCutScreen1, shortCutScreen2)
                        }
                    )
                } else {
                    ShortcutManagerCompat.removeAllDynamicShortcuts(context)
                }
            }
        } catch (_: Exception) {
        }

    }
}