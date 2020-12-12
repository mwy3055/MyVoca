package hsk.practice.myvoca.services.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val restartIntent = Intent(context, RestartService::class.java)
            context.startForegroundService(restartIntent)
        } else {
            val restartIntent = Intent(context, ShowNotificationService::class.java)
            context.startService(restartIntent)
        }
    }
}