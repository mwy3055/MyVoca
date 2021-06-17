package hsk.practice.myvoca.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import hsk.practice.myvoca.MyVocaPreferences
import hsk.practice.myvoca.databinding.VersusViewBinding
import hsk.practice.myvoca.getPreferences
import hsk.practice.myvoca.setPreferenceValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Custom view which compares two values and show them with bar graph.
 * Hard to explain in text, just see the design!
 */
class VersusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val _left = MutableStateFlow(0)
    val left: StateFlow<Int>
        get() = _left

    private val _right = MutableStateFlow(0)
    val right: StateFlow<Int>
        get() = _right

    val binding = VersusViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
        data = VersusViewData(left, right)
    }

    init {
        launch {
            _left.value = context.getPreferences(MyVocaPreferences.quizCorrectKey, 0)
            _right.value = context.getPreferences(MyVocaPreferences.quizWrongKey, 0)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        saveValues()
    }

    fun setLeftValue(value: Int) {
        if (value >= 0) {
            _left.value = value
        }
    }

    fun setRightValue(value: Int) {
        if (value >= 0) {
            _right.value = value
        }
    }

    fun increaseLeftValue() {
        setLeftValue(left.value + 1)
    }

    fun increaseRightValue() {
        setRightValue(right.value + 1)
    }

    private fun saveValues() = launch {
        context.setPreferenceValue(MyVocaPreferences.quizCorrectKey, left.value)
        context.setPreferenceValue(MyVocaPreferences.quizWrongKey, right.value)
    }

}

class VersusViewData(
    val left: StateFlow<Int> = MutableStateFlow(0),
    val right: StateFlow<Int> = MutableStateFlow(0)
)