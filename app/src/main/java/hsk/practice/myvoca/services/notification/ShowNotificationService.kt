package hsk.practice.myvoca.services.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocabularyImpl
import hsk.practice.myvoca.framework.toVocabularyImpl
import hsk.practice.myvoca.module.RoomVocaRepository
import hsk.practice.myvoca.ui.activity.MainActivity
import hsk.practice.myvoca.ui.activity.SplashActivity
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ShowNotificationService : LifecycleService() {
    private var receiver: ShowVocaNotificationReceiver? = null
    private var notificationManager: NotificationManager? = null

    //    private var vocaRepository: VocaRepository? = null
    @RoomVocaRepository
    @Inject
    lateinit var vocaRepository: VocaRepository

    private val VOCA_NOTIFICATION_CHANNEL_ID: String = "VOCA_NOTIFICATION"
    private val VOCA_NOTIFICATION_CHANNEL_NAME: String = "단어 보이기"

    private val VOCA_NOTIFICATION_ID: String = "1"
    private val NOTIFICATION_ID = 20

    private val SHOW_RANDOM_VOCA: String = "다른 단어"
    private val SHOW_RANDOM_VOCA_ACTION_NAME: String = "action.showvoca.notification"
    private val SHOW_RANDOM_VOCA_ACTION_ID = 200

    private val START_APP_ACTION_NAME: String = "action.showvoca.startapp"
    private val START_APP_ACTION_ID = 201
    private val RESTART_SERVICE_CODE = 220

    companion object {
        private var isRunning = false
        const val SHOW_VOCA: String = "단어"
        fun isRunning(): Boolean {
            return isRunning
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d("ShowNotificationService onCreate()")
//        AppHelper.loadInstance()

//        vocaViewModel = VocaViewModel()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        receiver = ShowVocaNotificationReceiver()

//        vocaRepository = VocaRepository(VocaPersistenceDatabase.getInstance(applicationContext))

        val filter = IntentFilter()
        filter.addAction(SHOW_RANDOM_VOCA_ACTION_NAME)
        filter.addAction(START_APP_ACTION_NAME)
        registerReceiver(receiver, filter)
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("ShowNotificationService onStartCommand()")
//        AppHelper.loadInstance()
        isRunning = true
//        val isEmpty = newVocaViewModel?.isEmpty()
//        isEmpty?.observeForever(object : Observer<Boolean?> {
//            override fun onChanged(aBoolean: Boolean?) {
//                if (intent?.getSerializableExtra(SHOW_VOCA) != null) {
//                    val vocabulary = intent.getSerializableExtra(SHOW_VOCA) as RoomVocabulary
//                    showWordOnNotification(vocabulary)
//                    Log.d("HSK APP", "show voca on notification: " + vocabulary.eng)
//                } else if (intent != null && !intent.getBooleanExtra(RestartService.RESTART_SERVICE, false) && aBoolean == false) {
//                    // only when starting with app and database not empty
//                    showRandomWordOnNotification()
//                    Log.d("HSK APP", "show random word on notification")
//                } else if (intent != null && intent.getBooleanExtra(RestartService.RESTART_SERVICE, false)) {
//                    Log.d("HSK APP", "restart notification service")
//                    setAlarmTimer()
//                }
//                isEmpty.removeObserver(this)
//            }
//        })
        lifecycleScope.launch {
            vocaRepository.getAllVocabulary().asLiveData(lifecycleScope.coroutineContext)
                .observeForever {
                    if (intent?.getSerializableExtra(SHOW_VOCA) != null) {
                        val vocabulary = intent.getSerializableExtra(SHOW_VOCA) as VocabularyImpl
                        showWordOnNotification(vocabulary)
                    }
                }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Logger.d("ShowNotificationService onDestroy()")
        lifecycleScope.launch {
//            if (vocaRepository?.getAllVocabulary()?.receive()?.isEmpty() == false) {
//                setAlarmTimer()
//            }
            setAlarmTimer()
        }
        unregisterReceiver(receiver)
    }

    /* set alarm timer to restart this service */
    private fun setAlarmTimer() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MILLISECOND, 500)

        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val sendAlarmPI =
            PendingIntent.getBroadcast(applicationContext, RESTART_SERVICE_CODE, intent, 0)
        val manager = getSystemService(ALARM_SERVICE) as AlarmManager
        manager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = sendAlarmPI
    }

    /* methods for showing notification */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VOCA_NOTIFICATION_CHANNEL_ID,
                VOCA_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.setSound(null, null)
            channel.setShowBadge(false)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun getBuilder(vocabulary: VocabularyImpl): NotificationCompat.Builder? {
        val startAppIntent = Intent(START_APP_ACTION_NAME)
        val startAppPI =
            PendingIntent.getBroadcast(applicationContext, START_APP_ACTION_ID, startAppIntent, 0)
        val showVocaIntent = Intent(SHOW_RANDOM_VOCA_ACTION_NAME)
        val showVocaPI = PendingIntent.getBroadcast(
            applicationContext,
            SHOW_RANDOM_VOCA_ACTION_ID,
            showVocaIntent,
            0
        )
        val action =
            NotificationCompat.Action(R.drawable.thinking_face, SHOW_RANDOM_VOCA, showVocaPI)
        return NotificationCompat.Builder(applicationContext, VOCA_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.baseline_bookmark_border_24)
            .setContentTitle(vocabulary.eng)
            .setContentText(vocabulary.kor)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setChannelId(VOCA_NOTIFICATION_CHANNEL_ID)
            .addAction(action)
            .setContentIntent(startAppPI)
    }

    private fun showRandomWordOnNotification() = lifecycleScope.launch {
        val vocabulary = vocaRepository.getRandomVocabulary()?.toVocabularyImpl() ?: return@launch
        showWordOnNotification(vocabulary)
    }

    private fun showWordOnNotification(vocabulary: VocabularyImpl) {
        createNotificationChannel()
        val builder = getBuilder(vocabulary)
        notificationManager?.notify(NOTIFICATION_ID, builder?.build())
    }

    inner class ShowVocaNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.d("NotificationReceiver onReceive()")
            if (intent?.action.equals(SHOW_RANDOM_VOCA_ACTION_NAME, ignoreCase = true)) {
                // TODO: wait until vocaViewModel is not null
                Logger.d("Show voca on notification")
                showRandomWordOnNotification()
            } else if (intent?.action.equals(START_APP_ACTION_NAME, ignoreCase = true)
                && !MainActivity.isRunning
            ) {
                Logger.d("Start app")
                val startAppIntent = Intent(applicationContext, SplashActivity::class.java)
                startAppIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(startAppIntent)
            }
        }
    }


}