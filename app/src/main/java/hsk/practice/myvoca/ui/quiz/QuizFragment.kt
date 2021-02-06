package hsk.practice.myvoca.ui.quiz

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hsk.practice.myvoca.PreferenceManager
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentQuizBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.ui.NewVocaViewModel
import hsk.practice.myvoca.ui.NewVocaViewModelFactory
import kotlin.random.Random

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
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null

    private val binding
        get() = _binding!!

    private val noVocaLayout
        get() = binding.layoutNoVoca

    private val curVoca
        get() = binding.textViewCurVoca

    private val quizLayout
        get() = binding.quizLayout

    private val quizWord
        get() = binding.quizWord

    private val versusView
        get() = binding.versusView

    private lateinit var newVocaViewModel: NewVocaViewModel

    var quizOptionsList: MutableList<TextView?>? = null

    var answerVoca: RoomVocabulary? = null
    var answerIndex = 0
    var answerCount = 0
    var wrongCount = 0
    lateinit var handler: Handler
    lateinit var showQuizRunnable: Runnable

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)

        newVocaViewModel = ViewModelProvider(this, NewVocaViewModelFactory(VocaPersistenceDatabase(context))).get(NewVocaViewModel::class.java)

        quizOptionsList = mutableListOf(binding.quizOption1, binding.quizOption2, binding.quizOption3, binding.quizOption4)
        quizOptionsList?.forEachIndexed { index, option ->
            option?.setOnClickListener { quizItemSelected(index) }
        }

        answerCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_CORRECT)
        wrongCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_WRONG)
        binding.versusView.setValues(answerCount, wrongCount)

        handler = Handler()
        showQuizRunnable = Runnable {
            val vocaCount = newVocaViewModel.getVocabularyCount()
            vocaCount?.observe(viewLifecycleOwner) { count ->
                if (count > 4) {
                    hideEmptyVocaLayout()
                    showQuizLayout()
                    showQuizWord()
                } else {

                    hideQuizLayout()
                    showEmptyVocaLayout(count)
                }
            }
        }

        tryShowQuizWord()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        tryShowQuizWord()
    }

    private fun showEmptyVocaLayout(vocaCount: Int) {
        noVocaLayout.visibility = View.VISIBLE
        curVoca.text = getString(R.string.current_voca_count, vocaCount)
    }

    private fun hideEmptyVocaLayout() {
        noVocaLayout.visibility = View.GONE
        curVoca.visibility = View.GONE
    }

    private fun showQuizLayout() {
        quizLayout.visibility = View.VISIBLE
        quizWord.visibility = View.VISIBLE
    }

    private fun hideQuizLayout() {
        quizLayout.visibility = View.GONE
        quizWord.visibility = View.GONE
    }

    private fun tryShowQuizWord() {
        handler.postDelayed(showQuizRunnable, 50)
    }

    fun showQuizWord() {
        newVocaViewModel.getRandomVocabularies(4).observe(viewLifecycleOwner) {
            val answerIndex = Random.nextInt(0, 4)
            val answer = it[answerIndex]

            this.answerIndex = answerIndex
            answerVoca = answer

            quizWord.text = answer?.eng
            it.forEachIndexed { index, optionVocabulary ->
                quizOptionsList?.get(index)?.text = getString(R.string.quiz_option_format, index + 1, optionVocabulary?.kor)
            }
        }
//        val answerLiveData = newVocaViewModel.getRandomVocabulary()
//        answerLiveData.observe(viewLifecycleOwner) { answer ->
//            val optionsList = newVocaViewModel.getRandomVocabularies(3, answer).toMutableList()
//            optionsList.add(answer)
//            if (answer != null) {
//                quizWord.text = answer.eng
//            }
//
//            optionsList.shuffle()
//            for (i in 0..3) {
//                val option = optionsList[i]
//                if (answer == option) {
//                    answerVoca = option
//                    answerIndex = i
//                }
//                quizOptionsList?.get(i)?.text = getString(R.string.quiz_option_format, i + 1, formatString(option?.kor))
//            }
//        }
    }

    // replace new line character to the space
    fun formatString(str: String?) = str?.replace("\n", " ")

    fun quizItemSelected(index: Int) {
        val isCorrect: Boolean
        val context = context
        if (answerIndex == index) {
            isCorrect = true
            answerCount++
            PreferenceManager.setInt(context, PreferenceManager.QUIZ_CORRECT, answerCount)
            versusView.setLeftValue(answerCount)
            //            Toast.makeText(context, String.format("정답: %d", answerCount), Toast.LENGTH_LONG).show();
        } else {
            isCorrect = false
            wrongCount++
            PreferenceManager.setInt(context, PreferenceManager.QUIZ_WRONG, wrongCount)
            versusView.setRightValue(wrongCount)
            //            Toast.makeText(context, String.format("오답: %d", wrongCount), Toast.LENGTH_LONG).show();
        }
        showVocaDialog(answerVoca, isCorrect)
        showQuizWord()
    }

    // shows whether the selection is correct
    fun showVocaDialog(voca: RoomVocabulary?, isCorrect: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (isCorrect) "맞았습니다!!" else "틀렸습니다")
        builder.setMessage(String.format("%s: %s", voca!!.eng, formatString(voca.kor)))
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
    }
}