package hsk.practice.myvoca.ui.seeall.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import hsk.practice.myvoca.databinding.VocaViewBinding

/**
 * Custom view which shows a vocabulary object in the database.
 * Shows english word, korean meaning and the last-edited time.
 */
class VocaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = VocaViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val vocaLayout
        get() = binding.vocaLayout

    private val vocaKor
        get() = binding.vocaKor

    private val vocaEng
        get() = binding.vocaEng

    private val lastEditTime
        get() = binding.lastEditTime

    val deleteCheckBox
        get() = binding.deleteCheckBox
}