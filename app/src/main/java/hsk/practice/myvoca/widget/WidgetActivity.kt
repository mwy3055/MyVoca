package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.R
import hsk.practice.myvoca.dataStore
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding
import hsk.practice.myvoca.framework.toRoomVocabulary
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

private const val widgetTextColorKey = "widget_text_color"
private val TEXT_COLOR_KEY = intPreferencesKey(widgetTextColorKey)

fun getTextColorFlow(context: Context): Flow<Int> = context.dataStore.data.map { preferences ->
    preferences[TEXT_COLOR_KEY] ?: ContextCompat.getColor(
        context,
        R.color.material_light_primary_text
    )
}

@AndroidEntryPoint
class WidgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetSettingBinding

    @RoomVocaRepository
    @Inject
    lateinit var vocaRepository: VocaRepository

    private var widgetId: Int = 0

    private var textColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setColorPickerSize()
        setProgressSize()

        setColorPickerVisibility(View.VISIBLE)
        setProgressVisibility(View.GONE)
        setTextButtonVisibility(View.GONE)

        binding.buttonClose.setOnClickListener { closeActivity() }
        binding.imageColorPreview.setOnClickListener { showColorPicker() }
        binding.buttonColorComplete.setOnClickListener {
            lifecycleScope.launch {
                saveTextColor()
            }
            setProgressVisibility(View.VISIBLE)
            setColorPickerVisibility(View.GONE)
            showWidget()
        }

        val textColorFlow = getTextColorFlow(this)
        lifecycleScope.launch {
            textColorFlow.take(1).collectLatest { color ->
                binding.imageColorPreview.setBackgroundColor(color)
                textColor = color
            }
        }

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    /**
     * Widget configuration Activity가 정의되었으므로,
     * 위젯을 생성한 후 최초의 업데이트는 여기에서 수행해야 한다.
     */
    private fun showWidget() = lifecycleScope.launch {
        val waitingJob = launch { delay(800L) }
        launch {
            Logger.d("WidgetActivity showWidget()")
            val context = this@WidgetActivity
            val widgetManager = AppWidgetManager.getInstance(context)

            val vocabulary =
                vocaRepository.getRandomVocabulary()?.toRoomVocabulary()
                    ?: throw IllegalStateException("Vocabulary is null!")
            VocaWidgetProvider.remoteViewWithVocabulary(context, widgetId, vocabulary)
                .also { remoteViews ->
                    with(remoteViews) {
                        setTextColor(R.id.widget_eng, textColor)
                        setTextColor(R.id.widget_kor, textColor)
                    }
                    widgetManager.updateAppWidget(widgetId, remoteViews)
                }
            waitingJob.join()
            setProgressVisibility(View.GONE)
            setTextButtonVisibility(View.VISIBLE)
        }
    }

    private fun closeActivity() {
        val result = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun showColorPicker() {
        ColorPickerDialogBuilder.with(this)
            .setTitle("텍스트 색깔")
            .initialColor(textColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(10)
            .setPositiveButton("확인") { _, lastSelectedColor, _ ->
                textColor = lastSelectedColor
                binding.imageColorPreview.setBackgroundColor(lastSelectedColor)
            }
            .setNegativeButton("취소") { _, _ -> }
            .build()
            .show()
    }

    suspend fun saveTextColor() {
        this.dataStore.edit { settings ->
            settings[TEXT_COLOR_KEY] = textColor
        }
    }

    private fun setColorPickerSize() {
        val metrics = resources.displayMetrics
        val params = binding.imageColorPreview.layoutParams
        params.width = metrics.widthPixels / 2
        params.height = metrics.widthPixels / 11
    }

    private fun setProgressSize() {
        val metrics = resources.displayMetrics
        val size = min(metrics.widthPixels, metrics.heightPixels) / 2
        val params = binding.progressLoading.layoutParams
        params.width = size
        params.height = size
    }

    private fun setColorPickerVisibility(visibility: Int) {
        with(binding) {
            textColorPick.visibility = visibility
            imageColorPreview.visibility = visibility
            buttonColorComplete.visibility = visibility
        }
    }

    private fun setProgressVisibility(visibility: Int) {
        binding.progressLoading.visibility = visibility
    }

    private fun setTextButtonVisibility(visibility: Int) {
        with(binding) {
            textAdd.visibility = visibility
            buttonClose.visibility = visibility
        }
    }
}