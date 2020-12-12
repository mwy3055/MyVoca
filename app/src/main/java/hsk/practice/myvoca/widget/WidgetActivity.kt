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

class WidgetActivity : AppCompatActivity() {
    private var vocaViewModel: VocaViewModel? = null
    private var widgetId = 0
    private val eng: TextView? = null
    private val kor: TextView? = null
    private val showVocaButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_setting)
        AppHelper.loadInstance(this)
        vocaViewModel = ViewModelProvider(this).get(VocaViewModel::class.java)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val allVocabulary = vocaViewModel.getAllVocabulary()
        allVocabulary.observeForever(object : Observer<MutableList<Vocabulary?>?> {
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
                remoteView.setTextViewText(R.id.widget_eng, vocabulary.eng)
                remoteView.setTextViewText(R.id.widget_kor, vocabulary.kor)
                widgetManager.updateAppWidget(widgetId, remoteView)
                val intent = Intent()
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                setResult(RESULT_OK, intent)
                finish()
                randomVocabulary.removeObserver(this)
            }
        })
    }
}