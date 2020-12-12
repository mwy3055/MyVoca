package hsk.practice.myvoca.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import database.Vocabulary
import database.source.VocaRepository
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.services.notification.ShowNotificationService

/**
 * SplashActivity shows splash screen, while preparing the database in the same time.
 * Also requires storage permission only one time to interact with the database.
 * When database is loaded, MainActivity is shown.
 */
class SplashActivity : AppCompatActivity() {
    private val permissionRequestCode = 1
    private val handler: Handler? = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
        AppHelper.loadInstance(this)
        // when database is loaded...
        VocaRepository.Companion.getInstance().getAllVocabulary().observe(this, Observer<MutableList<Vocabulary?>?> {
            startVocaProviderService()
            handler.post(Runnable {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            })
        })
    }

    private fun getPermissions() {
        for (permission in AppHelper.getPermissionList()) {
            val check = ContextCompat.checkSelfPermission(applicationContext, permission)
            if (check == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), permissionRequestCode)
            }
        }
    }

    private fun startVocaProviderService() {
        if (!ShowNotificationService.Companion.isRunning()) {
            val intent = Intent(applicationContext, ShowNotificationService::class.java)
            startService(intent)
        }
    }
}