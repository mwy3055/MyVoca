package hsk.practice.myvoca.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentQuizBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Shows word quiz to user.
 * There should be at least 5 words in the database to make the quiz shown.
 * When the word is not enough, notification text will be shown instead.
 * User should choose the correct meaning of the quiz word. Four options will be provided.
 * When user selects the option, quiz word and the VersusView at the bottom of the fragment will be updated.
 *
 *
 * Numbers of correct and wrong answers are stored in the SharedPreferences and updated in real time.
 */
@AndroidEntryPoint
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding
        get() = _binding!!

    private val quizViewModel: QuizViewModel by viewModels()

    private lateinit var quizOptionsList: MutableList<TextView>

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner

        quizOptionsList = mutableListOf(binding.quizOption1, binding.quizOption2, binding.quizOption3, binding.quizOption4)
        quizOptionsList.forEachIndexed { index, option ->
            option.setOnClickListener { quizViewModel.quizItemSelected(requireContext(), index) }
        }

        setVersusView()

        // Do not execute any code while observing quizAvailable
        quizViewModel.quizAvailable.observe(viewLifecycleOwner) { }
        quizViewModel.quizLoadCompleteEvent.observe(viewLifecycleOwner) { loadResult ->
            Logger.d("Quiz load status: $loadResult")
            loadResult?.let {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    if (it) showQuiz(quizViewModel.answerVoca.value!!, quizViewModel.quizVocabulary.value!!) else hideQuiz()
                    quizViewModel.clearQuizPreparedEvent()
                }
            }
        }
        quizViewModel.answerEvent.observe(viewLifecycleOwner) { value ->
            value?.let {
                setVersusView()
                showVocaDialog(quizViewModel.answerVoca.value!!, it)
                quizViewModel.clearAnswerEvent()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setVersusView() {
        quizViewModel.loadPreferences(requireContext())
        binding.versusView.setValues(quizViewModel.answerCount, quizViewModel.wrongCount)
    }

    private fun showEmptyVoca() {
        with(binding) {
            noVocaText.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyVoca() {
        with(binding) {
            noVocaText.visibility = View.GONE
        }
    }

    private fun showQuizLayout() {
        with(binding) {
            quizLayout.visibility = View.VISIBLE
            quizWord.visibility = View.VISIBLE
        }
    }

    private fun hideQuizLayout() {
        with(binding) {
            quizLayout.visibility = View.GONE
            quizWord.visibility = View.GONE
        }
    }

    private fun showQuiz(answerVoca: RoomVocabulary, quizVocabulary: List<RoomVocabulary>) {
        hideEmptyVoca()
        showQuizLayout()
        with(binding) {
            Logger.d("Quiz word: ${answerVoca.eng}")
            quizWord.text = answerVoca.eng
            Logger.d("Quiz Vocabulary content: $quizVocabulary")
            quizVocabulary.forEachIndexed { index, vocabulary ->
                quizOptionsList[index].text = getString(R.string.quiz_option_format, index + 1, vocabulary.kor)
            }
            Logger.d("Quiz layout visibility: ${quizLayout.visibility == View.VISIBLE}")
        }
    }

    private fun hideQuiz() {
        hideQuizLayout()
        showEmptyVoca()
    }

    // replace new line character to the space
    fun formatString(str: String?) = str?.replace("\n", " ")

    // TODO: Methods below should be moved into QuizViewModel
    fun showVocaDialog(voca: RoomVocabulary, isCorrect: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (isCorrect) "맞았습니다!!" else "틀렸습니다")
        builder.setMessage("${voca.eng}: ${formatString(voca.kor)}")
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
    }
}