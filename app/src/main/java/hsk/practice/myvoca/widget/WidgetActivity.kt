package hsk.practice.myvoca.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.databinding.ActivityWidgetSettingBinding
import hsk.practice.myvoca.framework.toRoomVocabulary
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetSettingBinding

    @RoomVocaRepository
    @Inject
    lateinit var vocaRepository: VocaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        showWidget(widgetId)
    }

    /**
     * Widget configuration Activity가 정의되었으므로,
     * 위젯을 생성한 후 최초의 업데이트는 여기에서 수행해야 한다.
     */
    private fun showWidget(widgetId: Int) = lifecycleScope.launch {
        Logger.d("WidgetActivity showWidget()")
        val context = this@WidgetActivity
        val widgetManager = AppWidgetManager.getInstance(context)

        val vocabulary = vocaRepository.getRandomVocabulary()?.toRoomVocabulary() ?: return@launch
        VocaWidgetProvider.remoteViewWithVocabulary(context, widgetId, vocabulary)
            .also { remoteViews ->
                widgetManager.updateAppWidget(widgetId, remoteViews)
            }

        val result = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}