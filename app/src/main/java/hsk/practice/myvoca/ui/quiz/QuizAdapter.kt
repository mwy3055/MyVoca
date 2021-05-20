package hsk.practice.myvoca.ui.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.QuizItemBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.ui.seeall.recyclerview.RoomVocabularyDiffCallback

class QuizAdapter : ListAdapter<RoomVocabulary, QuizViewHolder>(RoomVocabularyDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder =
        QuizViewHolder.from(parent)

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
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

    fun bind(quizOption: RoomVocabulary, position: Int) {
        binding.position = position
        binding.quizOption = quizOption
    }
}

@BindingAdapter(value = ["position", "quizOption"], requireAll = true)
fun TextView.bindQuiz(position: Int, quizOption: RoomVocabulary) {
    val quizText = context.getString(R.string.quiz_option_format, position, quizOption.kor)
    text = quizText
    Logger.d("Quiz text set to $quizText")
}

