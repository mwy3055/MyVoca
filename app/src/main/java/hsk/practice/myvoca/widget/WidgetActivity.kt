package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding
import hsk.practice.myvoca.framework.toRoomVocabulary
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class WidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetSettingBinding

    @RoomVocaRepository
    @Inject
    lateinit var vocaRepository: VocaRepository

    private var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonClose.setOnClickListener {
            closeActivity()
        }

        setProgressSize()
        setProgressVisibility(View.VISIBLE)
        setTextButtonVisibility(View.GONE)

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        showWidget()
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

    private fun setProgressSize() {
        val metrics = resources.displayMetrics
        val size = min(metrics.widthPixels, metrics.heightPixels) / 2
        val params = binding.progressLoading.layoutParams
        params.width = size
        params.height = size
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