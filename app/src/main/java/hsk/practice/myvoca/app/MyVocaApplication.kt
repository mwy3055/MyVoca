package hsk.practice.myvoca.app

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyVocaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        loadLogger()
    }

    private fun loadLogger() {
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}