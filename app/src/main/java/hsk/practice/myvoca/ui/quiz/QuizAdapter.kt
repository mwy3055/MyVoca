package hsk.practice.myvoca.ui.quiz

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.QuizItemBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.ui.seeall.recyclerview.RoomVocabularyDiffCallback

class QuizAdapter : ListAdapter<RoomVocabulary, QuizViewHolder>(RoomVocabularyDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder =
        QuizViewHolder.from(parent)

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}

class ItemDecoration(private val size: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top += size
        }
    }
}

class QuizViewHolder private constructor(private val binding: QuizItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): QuizViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = QuizItemBinding.inflate(layoutInflater, parent, false)
            return QuizViewHolder(binding)
        }
    }

    fun bind(quizOption: RoomVocabulary) {
        binding.quizOption = quizOption
    }
}

@BindingAdapter("quizOption")
fun TextView.bindQuiz(quizOption: RoomVocabulary) {
    val basicText =
        (quizOption.kor ?: context.getString(R.string.quiz_option_no_text, quizOption.eng)).replace(
            '\n',
            ' '
        )
    text = basicText.takeIf { it.length > 15 }?.apply { basicText.substring(0..15).plus("...") }
        ?: basicText
}

