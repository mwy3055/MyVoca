package hsk.practice.myvoca.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import hsk.practice.myvoca.R

/**
 * Custom view which compares two values and show proper graphics.
 * Hard to explain in text, just see the QuizFragment!
 */
class VersusView : LinearLayout {
    private val BAR_HEIGHT = 50
    private var leftValue = 0
    private var rightValue = 0
    var leftTextView: TextView? = null
    var rightTextView: TextView? = null
    var leftBar: View? = null
    var rightBar: View? = null

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.versus_view, this, true)
        leftTextView = findViewById(R.id.count_left)
        rightTextView = findViewById(R.id.count_right)
        leftBar = findViewById(R.id.left_bar)
        rightBar = findViewById(R.id.right_bar)
    }

    fun setValues(left: Int, right: Int) {
        setLeftValue(left)
        setRightValue(right)
    }

    fun setLeftValue(value: Int) {
        if (value < 0) {
            return
        }
        leftValue = value
        leftTextView.setText(Integer.toString(leftValue))
        refreshView()
    }

    fun setRightValue(value: Int) {
        if (value < 0) {
            return
        }
        rightValue = value
        rightTextView.setText(Integer.toString(rightValue))
        refreshView()
    }

    fun refreshView() {
        // Weights should be set oppositely to get correct result
        leftBar.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, rightValue))
        rightBar.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, leftValue))
    }
}