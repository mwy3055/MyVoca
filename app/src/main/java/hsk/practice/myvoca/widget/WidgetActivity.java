package hsk.practice.myvoca.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import Database.Vocabulary;
import hsk.practice.myvoca.AppHelper;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;

public class WidgetActivity extends AppCompatActivity {
    private VocaViewModel vocaViewModel;
    private int widgetId;

    private TextView eng;
    private TextView kor;
    private Button showVocaButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_setting);

        AppHelper.loadInstance(this);
        vocaViewModel = new ViewModelProvider(this).get(VocaViewModel.class);

        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        final LiveData<List<Vocabulary>> allVocabulary = vocaViewModel.getAllVocabulary();
        allVocabulary.observeForever(new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                showWidget();
                allVocabulary.removeObserver(this);
            }
        });
    }

    private void showWidget() {
        Log.d("HSK APP", "WidgetActivity showWidget()");
        final LiveData<Vocabulary> randomVocabulary = vocaViewModel.getRandomVocabulary();
        if (randomVocabulary == null || randomVocabulary.getValue() == null) {
            Log.d("HSK APP", "Vocabulary on widget NULL");
            return;
        }
        randomVocabulary.observeForever(new Observer<Vocabulary>() {
            @Override
            public void onChanged(Vocabulary vocabulary) {
                AppWidgetManager widgetManager = AppWidgetManager.getInstance(WidgetActivity.this);
                RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.widget_layout);
                remoteView.setTextViewText(R.id.widget_eng, vocabulary.eng);
                remoteView.setTextViewText(R.id.widget_kor, vocabulary.kor);
                widgetManager.updateAppWidget(widgetId, remoteView);

                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                setResult(Activity.RESULT_OK, intent);
                finish();

                randomVocabulary.removeObserver(this);
            }
        });
    }
}
