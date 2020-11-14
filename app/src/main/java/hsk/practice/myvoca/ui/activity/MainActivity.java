package hsk.practice.myvoca.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import hsk.practice.myvoca.Constants;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.services.notification.ShowNotificationService;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;

    private static boolean isRunning = false;

    @Override
    protected synchronized void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("HSK APP", "MainActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddVocaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, Constants.CALL_ADD_VOCA_ACTIVITY);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_see_all, R.id.nav_quiz,
                R.id.nav_tools, R.id.nav_share, R.id.nav_go_blog)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // TODO: look if battery permission is required to background service
        // result: required
        // getIgnoreBatteryOptPermission();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("HSK APP", "MainActivity onDestroy()");

        if (ShowNotificationService.isRunning()) {
            stopService(new Intent(getApplicationContext(), ShowNotificationService.class));
        }
        super.onDestroy();
    }

    @Override
    protected synchronized void onStop() {
        super.onStop();
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void getIgnoreBatteryOptPermission() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isWhiteListed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isWhiteListed = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        if (!isWhiteListed) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}
