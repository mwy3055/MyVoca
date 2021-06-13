package hsk.practice.myvoca.app

import android.app.Application
import androidx.preference.PreferenceManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import hsk.practice.myvoca.R
import hsk.practice.myvoca.setNightMode

@HiltAndroidApp
class MyVocaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        loadLogger()
        setDarkModeLayout()
    }

    private fun loadLogger() {
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    private fun setDarkModeLayout() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val darkMode = sharedPreferences.getBoolean(getString(R.string.settings_dark_mode), false)
        setNightMode(darkMode)
    }
}