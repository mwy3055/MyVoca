package hsk.practice.myvoca.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import java.util.List;

import Database.Vocabulary;
import Database.source.VocaRepository;
import hsk.practice.myvoca.AppHelper;
import hsk.practice.myvoca.services.notification.ShowNotificationService;

public class SplashActivity extends AppCompatActivity {

    private int permissionRequestCode = 1;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPermissions();

        AppHelper.loadInstance(this);
        VocaRepository.getInstance().getAllVocabulary().observe(this, new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                startVocaProviderService();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private void getPermissions() {
        for (String permission : AppHelper.getPermissionList()) {
            int check = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (check == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, permissionRequestCode);
            }
        }
    }

    private void startVocaProviderService() {
        if (!ShowNotificationService.isRunning()) {
            Intent intent = new Intent(getApplicationContext(), ShowNotificationService.class);
            startService(intent);
        }
    }
}
