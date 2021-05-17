package hsk.practice.myvoca.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import hsk.practice.myvoca.databinding.VersusViewBinding

/**
 * Custom view which compares two values and show them with bar graph.
 * Hard to explain in text, just see the design!
 */
class VersusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = VersusViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
        viewModel = VersusViewModel()
    }

    val viewModel: VersusViewModel
        get() = binding.viewModel!!

}