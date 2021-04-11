package hsk.practice.myvoca.ui.seeall

import android.widget.TextView
import androidx.databinding.BindingAdapter
import hsk.practice.myvoca.framework.RoomVocabulary

@BindingAdapter("vocaNumber")
fun TextView.setVocaNumber(vocabularies: List<RoomVocabulary?>?) {
    text = (vocabularies?.size ?: 0).toString()
}