package hsk.practice.myvoca.ui.seeall

import android.widget.TextView
import androidx.databinding.BindingAdapter
import hsk.practice.myvoca.VocabularyImpl

@BindingAdapter("vocaNumber")
fun TextView.setVocaNumber(vocabularies: List<VocabularyImpl?>?) {
    text = (vocabularies?.size ?: 0).toString()
}