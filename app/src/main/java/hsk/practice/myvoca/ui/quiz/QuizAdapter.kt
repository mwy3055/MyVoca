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

class QuizAdapter(private val onClick: (position: Int) -> Unit) :
    ListAdapter<RoomVocabulary, QuizAdapter.QuizViewHolder>(RoomVocabularyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuizItemBinding.inflate(inflater, parent, false)
        return QuizViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class QuizViewHolder(
        private val binding: QuizItemBinding,
        private val onClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onClicked(absoluteAdapterPosition)
            }
        }

        fun bind(quizOption: RoomVocabulary) {
            binding.quizOption = quizOption
        }
    }

}

class ItemDecoration(private var size: Int) : RecyclerView.ItemDecoration() {
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

    fun setSize(size: Int) {
        this.size = size
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

