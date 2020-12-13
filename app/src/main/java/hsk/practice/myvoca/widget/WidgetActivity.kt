package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import database.Vocabulary
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding

class WidgetActivity : AppCompatActivity() {
    private lateinit var binding:ActivityWidgetSettingBinding

    private lateinit var vocaViewModel: VocaViewModel
    private var widgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppHelper.loadInstance(this)
        vocaViewModel = ViewModelProvider(this).get(VocaViewModel::class.java)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val allVocabulary = vocaViewModel.getAllVocabulary()
        allVocabulary?.observeForever(object : Observer<MutableList<Vocabulary?>?> {
            override fun onChanged(vocabularies: MutableList<Vocabulary?>?) {
                showWidget()
                allVocabulary.removeObserver(this)
            }
        })
    }

    private fun showWidget() {
        Log.d("HSK APP", "WidgetActivity showWidget()")
        val randomVocabulary = vocaViewModel.getRandomVocabulary()
        if (randomVocabulary == null || randomVocabulary.value == null) {
            Log.d("HSK APP", "Vocabulary on widget NULL")
            return
        }
        randomVocabulary.observeForever(object : Observer<Vocabulary?> {
            override fun onChanged(vocabulary: Vocabulary?) {
                val widgetManager = AppWidgetManager.getInstance(this@WidgetActivity)

                val remoteView = RemoteViews(packageName, R.layout.widget_layout)
                remoteView.setTextViewText(R.id.widget_eng, vocabulary?.eng)
                remoteView.setTextViewText(R.id.widget_kor, vocabulary?.kor)
                widgetManager.updateAppWidget(widgetId, remoteView)

                val intent = Intent()
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                setResult(RESULT_OK, intent)
                randomVocabulary.removeObserver(this)

                finish()
            }
        })
    }
}