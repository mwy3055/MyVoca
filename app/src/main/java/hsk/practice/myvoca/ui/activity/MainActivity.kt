package hsk.practice.myvoca.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.ActivityMainBinding
import hsk.practice.myvoca.services.notification.ShowNotificationService
import hsk.practice.myvoca.ui.settings.SettingsActivity

/**
 * MainActivity shows major fragments of the application.
 * Follows the design pattern of Navigation Drawer Activity template.
 *
 * Fragment list: HomeFragment, SeeAllFragment, QuizFragment, GoBlogFragment(for fun!).
 * See res/navigation/mobile_navigation.xml for reference.
 * Other fragments were left for future features.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var drawer: DrawerLayout

    @Synchronized
    override fun onStart() {
        super.onStart()
        isRunning = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d("MainActivity onCreate()")
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val toolbar = binding.appBarMain.coordinatorLayout.findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // button on the right bottom.
        val fab = binding.appBarMain.fab
        fab.setOnClickListener {
            val intent = Intent(applicationContext, AddVocaActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivityForResult(intent, Constants.CALL_ADD_VOCA_ACTIVITY)
        }

        drawer = binding.drawerLayout
        val navigationView = binding.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_see_all, R.id.nav_quiz,
            R.id.nav_tools, R.id.nav_share, R.id.nav_go_github
        ).setOpenableLayout(drawer)
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
        Logger.d("MainActivity onDestroy()")
        if (ShowNotificationService.isRunning()) {
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
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }

    companion object {
        var isRunning = false
            private set
    }
}