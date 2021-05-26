package hsk.practice.myvoca.ui.customview

import android.widget.LinearLayout
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_weight")
fun setLayoutWeight(view: LinearLayout, value: Int?) {
    val params = view.layoutParams
    view.layoutParams =
        LinearLayout.LayoutParams(params.width, params.height, (value ?: 1).toFloat())
}