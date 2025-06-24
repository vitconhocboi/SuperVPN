package com.tici.vpn.proxy.master.scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.tici.vpn.proxy.master.broadcast.SchedulerReceiver
import androidx.core.net.toUri

object StopProxyScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun runProxy(context: Context, afterMinutes: Int, callback: () -> Unit) {
        if (afterMinutes > 0) {
            if (canScheduleExactAlarms(context)) {
                val triggerTime = System.currentTimeMillis() + afterMinutes * 60 * 1000
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, SchedulerReceiver::class.java)
                intent.action = SchedulerReceiver.STOP_PROXY
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                callback()
            } else {
                requestExactAlarmPermission(context)
            }
        } else {
            callback()
        }
    }

    private fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                ("package:" + context.packageName).toUri())
            context.startActivity(intent)
        }
    }

}