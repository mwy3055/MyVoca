package hsk.practice.myvoca.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.services.notification.ShowNotificationService

/**
 * MainActivity shows major fragments of the application.
 * Follows the design pattern of Navigation Drawer Activity template.
 *
 * Fragment list: HomeFragment, SeeAllFragment, QuizFragment, GoBlogFragment(for fun!).
 * See res/navigation/mobile_navigation.xml for reference.
 * Other fragments were left for future features.
 */
class MainActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var drawer: DrawerLayout? = null
    @Synchronized
    override fun onStart() {
        super.onStart()
        isRunning = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("HSK APP", "MainActivity onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // button on the right bottom.
        val fab = findViewById<FloatingActionButton?>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(applicationContext, AddVocaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivityForResult(intent, Constants.CALL_ADD_VOCA_ACTIVITY)
        }
        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView?>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_see_all, R.id.nav_quiz,
                R.id.nav_tools, R.id.nav_share, R.id.nav_go_blog)
                .setDrawerLayout(drawer)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navigationView, navController)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        Log.d("HSK APP", "MainActivity onDestroy()")
        if (ShowNotificationService.Companion.isRunning()) {
            stopService(Intent(applicationContext, ShowNotificationService::class.java))
        }
        super.onDestroy()
    }

    @Synchronized
    override fun onStop() {
        super.onStop()
        isRunning = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_empty, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }

    companion object {
        private var isRunning = false
        fun isRunning(): Boolean {
            return isRunning
        }
    }
}