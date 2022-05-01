package hsk.practice.myvoca.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import hsk.practice.myvoca.R
import hsk.practice.myvoca.util.setNightMode
import javax.inject.Inject

@HiltAndroidApp
class MyVocaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        loadLogger()
        setDarkModeLayout()
    }

    private fun loadLogger() {
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    private fun setDarkModeLayout() {
        val sharedPreferences = getDefaultSharedPreferences(applicationContext)
        val isDarkMode = sharedPreferences.getBoolean(getString(R.string.settings_dark_mode), false)
        setNightMode(isDarkMode)
    }

    private fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}