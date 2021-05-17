package hsk.practice.myvoca.ui.home

import android.widget.TextView
import androidx.databinding.BindingAdapter
import hsk.practice.myvoca.R

@BindingAdapter("vocabularySize")
fun bindVocabularySizeText(textView: TextView, size: Int) {
    textView.text = if (size > 0) {
        textView.context.getString(R.string.home_fragment_word_count, size)
    } else {
        textView.context.getString(R.string.home_fragment_no_voca)
    }
}