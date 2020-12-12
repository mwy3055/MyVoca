package hsk.practice.myvoca.services.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import hsk.practice.myvoca.R

class RestartService : Service() {
    private val RESTART_SERVICE_NOTIFICATION_ID = 10000
    private val RESTART_SERVICE_ID: String? = "restart"
    private val RESTART_SERVICE_NAME: String? = "일반"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(RESTART_SERVICE_ID, RESTART_SERVICE_NAME, NotificationManager.IMPORTANCE_LOW)
        channel.setSound(null, null)
        channel.setShowBadge(false)
        manager.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(applicationContext, "default")
                .setSmallIcon(R.drawable.thinking_face)
                .setChannelId(RESTART_SERVICE_ID)
        val notification = builder.build()
        startForeground(RESTART_SERVICE_NOTIFICATION_ID, notification)
        val restartServiceIntent = Intent(applicationContext, ShowNotificationService::class.java)
        restartServiceIntent.putExtra(RESTART_SERVICE, true)
        startService(restartServiceIntent)
        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }

    companion object {
        val RESTART_SERVICE: String? = "restart_service"
    }
}