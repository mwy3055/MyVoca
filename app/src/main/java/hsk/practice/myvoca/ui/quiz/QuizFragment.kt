package hsk.practice.myvoca.ui.quiz

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import database.PreferenceManager
import database.Vocabulary
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.ui.customview.VersusView
import java.util.*

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
    private var viewModelProvider: ViewModelProvider? = null
    private var quizViewModel: QuizViewModel? = null
    private var vocaViewModel: VocaViewModel? = null
    var noVocaLayout: RelativeLayout? = null
    var vocaCountText: TextView? = null
    var quizLayout: LinearLayout? = null
    var quizWordText: TextView? = null
    var quizOption1: TextView? = null
    var quizOption2: TextView? = null
    var quizOption3: TextView? = null
    var quizOption4: TextView? = null
    var quizOptionsList: MutableList<TextView?>? = null
    var versusView: VersusView? = null
    var answerVoca: Vocabulary? = null
    var answerIndex = 0
    var answerCount = 0
    var wrongCount = 0
    var handler: Handler? = null
    var showQuizRunnable: Runnable? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelProvider = ViewModelProvider(this)
        quizViewModel = viewModelProvider.get(QuizViewModel::class.java)
        vocaViewModel = viewModelProvider.get(VocaViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_quiz, container, false)
        handler = Handler()
        noVocaLayout = root.findViewById(R.id.layout_no_voca)
        vocaCountText = root.findViewById(R.id.text_view_cur_voca)
        quizLayout = root.findViewById(R.id.quiz_layout)
        quizWordText = root.findViewById(R.id.quiz_word)
        quizOption1 = root.findViewById(R.id.quiz_option1)
        quizOption2 = root.findViewById(R.id.quiz_option2)
        quizOption3 = root.findViewById(R.id.quiz_option3)
        quizOption4 = root.findViewById(R.id.quiz_option4)
        versusView = root.findViewById(R.id.versus_view)
        quizOptionsList = Arrays.asList(quizOption1, quizOption2, quizOption3, quizOption4)
        for (i in 0..3) {
            val option = quizOptionsList.get(i)
            option.setOnClickListener { quizItemSelected(i) }
        }
        answerCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_CORRECT)
        wrongCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_WRONG)
        versusView.setValues(answerCount, wrongCount)
        showQuizRunnable = Runnable {
            vocaViewModel.getVocabularyCount().observe(viewLifecycleOwner, { integer ->
                Log.d("HSK APP", integer.toString())
                if (integer > 4) {
                    hideEmptyVocaLayout()
                    showQuizLayout()
                    showQuizWord()
                } else {
                    hideQuizLayout()
                    showEmptyVocaLayout(integer)
                }
            })
        }
        tryShowQuizWord()
        return root
    }

    override fun onResume() {
        super.onResume()
        tryShowQuizWord()
    }

    fun showEmptyVocaLayout(vocaCount: Int) {
        noVocaLayout.setVisibility(View.VISIBLE)
        vocaCountText.setText(getString(R.string.current_voca_count, vocaCount))
    }

    fun hideEmptyVocaLayout() {
        noVocaLayout.setVisibility(View.GONE)
        vocaCountText.setVisibility(View.GONE)
    }

    fun showQuizLayout() {
        quizLayout.setVisibility(View.VISIBLE)
        quizWordText.setVisibility(View.VISIBLE)
    }

    fun hideQuizLayout() {
        quizLayout.setVisibility(View.GONE)
        quizWordText.setVisibility(View.GONE)
    }

    fun tryShowQuizWord() {
        handler.postDelayed(showQuizRunnable, 50)
    }

    fun showQuizWord() {
        val answer = vocaViewModel.getRandomVocabulary().value
        val optionsList = vocaViewModel.getRandomVocabularies(3, answer)
        optionsList.add(answer)
        try {
            quizWordText.setText(answer.eng)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        Collections.shuffle(optionsList)
        for (i in 0..3) {
            val option = optionsList[i]
            if (answer == option) {
                answerVoca = option
                answerIndex = i
            }
            quizOptionsList.get(i).setText(String.format("%d. %s", i + 1, formatString(option.kor)))
        }
    }

    // replace new line character to the space
    fun formatString(str: String?): String? {
        return str.replace("\n", " ")
    }

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
    fun showVocaDialog(voca: Vocabulary?, isCorrect: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (isCorrect) "맞았습니다!!" else "틀렸습니다")
        builder.setMessage(String.format("%s: %s", voca.eng, formatString(voca.kor)))
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
    }
}