package hsk.practice.myvoca.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import hsk.practice.myvoca.databinding.VersusViewBinding

/**
 * Custom view which compares two values and show proper graphics.
 * Hard to explain in text, just see the QuizFragment!
 */
class VersusView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = VersusViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val BAR_HEIGHT = 50
    private var leftValue = 0
    private var rightValue = 0

    fun setValues(left: Int, right: Int) {
        setLeftValue(left)
        setRightValue(right)
    }

    fun setLeftValue(value: Int) {
        if (value < 0) {
            return
        }
        leftValue = value
        binding.countLeft.text = leftValue.toString()
        refreshView()
    }

    fun setRightValue(value: Int) {
        if (value < 0) {
            return
        }
        rightValue = value
        binding.countRight.text = rightValue.toString()
        refreshView()
    }

    fun refreshView() {
        // Weights should be set oppositely to get correct result
        binding.leftBar.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, rightValue.toFloat())
        binding.rightBar.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, leftValue.toFloat())
    }
}