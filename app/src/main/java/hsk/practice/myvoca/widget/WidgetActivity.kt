package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding
import hsk.practice.myvoca.ui.NewVocaViewModel

class WidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetSettingBinding

    private lateinit var newVocaViewModel: NewVocaViewModel
    private var widgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppHelper.loadInstance(this)
        newVocaViewModel = ViewModelProvider(this).get(NewVocaViewModel::class.java)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        showWidget()
    }

    private fun showWidget() {
        Log.d("HSK APP", "WidgetActivity showWidget()")
        newVocaViewModel.getRandomVocabulary()?.let {
            val widgetManager = AppWidgetManager.getInstance(this@WidgetActivity)

            val remoteView = RemoteViews(packageName, R.layout.widget_layout)
            remoteView.setTextViewText(R.id.widget_eng, it.eng)
            remoteView.setTextViewText(R.id.widget_kor, it.kor)
            widgetManager.updateAppWidget(widgetId, remoteView)

            val intent = Intent()
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(RESULT_OK, intent)
            finish()
        } ?: run {
            Log.d("HSK APP", "Vocabulary on widget NULL")
        }
    }
}