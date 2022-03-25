package hsk.practice.myvoca.ui.screens.quiz

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.domain.VocaPersistence
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImplList
import hsk.practice.myvoca.util.MyVocaPreferencesKey
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.util.randoms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [ViewModel] for QuizFragment.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence,
    private val preferences: PreferencesDataStore
) : ViewModel() {

    private var quizAvailable: Boolean = false

    private val _quizScreenData = MutableStateFlow(QuizScreenData())
    val quizScreenData: StateFlow<QuizScreenData>
        get() = _quizScreenData

    private val resultDataFlow = MutableStateFlow<QuizResultData?>(null)

    init {
        viewModelScope.launch {
            combine(
                vocaPersistence.getAllVocabulary(),
                preferences.getPreferenceFlow(MyVocaPreferencesKey.quizCorrectKey, 0),
                preferences.getPreferenceFlow(MyVocaPreferencesKey.quizWrongKey, 0),
                resultDataFlow
            ) { allVocabulary, correct, wrong, quizResultData ->
                // Check if quiz data should be reloaded
                val value = quizScreenData.value

                val newQuizAvailable = allVocabulary.size >= vocabularyRequired
                val onlyResultChanged = quizResultData != null

                return@combine when {
                    // 결과값이 있는 경우 결과만 반환
                    onlyResultChanged -> {
                        quizAvailable = true
                        quizScreenData.value.copy(quizResult = quizResultData)
                    }
                    // 불가능 -> 가능
                    newQuizAvailable and !quizAvailable -> {
                        quizAvailable = true
                        makeNewQuiz(allVocabulary, correct, wrong)
                    }
                    // 가능 -> 가능: 무조건 로드해야 하는 것은 아님
                    newQuizAvailable -> {
                        // 전체 단어만 바뀌었다면 새로 로드하지 않음
                        if ((value.quizData.quizStat == QuizStat(correct, wrong)) and
                            (value.quizResult == quizResultData)
                        ) {
                            value.copy(numberVocabularyNeed = vocabularyRequired - allVocabulary.size)
                        } else {
                            // 뭔가 바뀌었을 경우 새로 로드
                            makeNewQuiz(allVocabulary, correct, wrong)
                        }
                    }
                    else -> {
                        // 퀴즈를 로드할 수 없는 경우
                        quizAvailable = false
                        QuizScreenData(
                            quizState = QuizNotAvailable,
                            numberVocabularyNeed = vocabularyRequired - allVocabulary.size,
                            quizData = QuizData(quizStat = QuizStat(correct, wrong))
                        )
                    }
                }
            }.collect {
                _quizScreenData.value = it
            }
        }
    }

    private fun makeNewQuiz(
        allVocabulary: List<Vocabulary>,
        correct: Int,
        wrong: Int
    ): QuizScreenData {
        val quizList = allVocabulary.randoms(quizSize).toVocabularyImplList()
        val answerIndex = (0 until quizSize).random()
        return QuizScreenData(
            quizState = QuizAvailable,
            numberVocabularyNeed = vocabularyRequired - allVocabulary.size,
            quizData = QuizData(
                quiz = Quiz(quizList = quizList, answerIndex = answerIndex),
                quizStat = QuizStat(correct, wrong)
            )
        )
    }

    fun onQuizOptionSelected(index: Int) {
        Logger.d("$index item clicked!")
        val quiz = quizScreenData.value.quizData.quiz
        val result = if (index == quiz.answerIndex) QuizCorrect else QuizWrong
        resultDataFlow.value = QuizResultData(result, quiz.answer)
    }

    fun onResultDialogClose(resultData: QuizResultData) {
        viewModelScope.launch {
            resultDataFlow.value = null

            val currentStat = quizScreenData.value.quizData.quizStat
            val (key, value) = if (resultData.result is QuizCorrect) {
                Pair(MyVocaPreferencesKey.quizCorrectKey, currentStat.correct + 1)
            } else {
                Pair(MyVocaPreferencesKey.quizWrongKey, currentStat.wrong + 1)
            }
            preferences.setPreference(key, value)
        }
    }

}

/**
 * Number of vocabulary in quiz
 */
val quizSize = 4

/**
 * Minimum number of vocabulary required to solve the quiz
 */
val vocabularyRequired = 10

/**
 * State of the quiz data.
 *
 * [QuizLoading] indicates that the data is currently loading
 * [QuizAvailable] indicates that the quiz is available.
 * [QuizNotAvailable] indicates that the quiz is not available (due to lack of vocabulary, etc.)
 */
sealed class QuizState

object QuizInit : QuizState()
object QuizLoading : QuizState()
object QuizAvailable : QuizState()
object QuizNotAvailable : QuizState()

/**
 * Result of the quiz.
 *
 * [QuizCorrect] indicates that the user chose the right answer.
 * [QuizWrong] indicates that the user failed the quiz.
 */
sealed class QuizResult

object QuizCorrect : QuizResult()
object QuizWrong : QuizResult()

@Immutable
data class QuizResultData(val result: QuizResult, val answer: VocabularyImpl)

@Immutable
data class Quiz(
    val quizList: List<VocabularyImpl> = emptyList(),
    val answerIndex: Int = 0
) {
    val answer: VocabularyImpl
        get() = quizList[answerIndex]
}

@Immutable
data class QuizStat(val correct: Int = 0, val wrong: Int = 0) {
    val valid: Boolean
        get() = (correct != 0) or (wrong != 0)
}

@Immutable
data class QuizData(
    val quiz: Quiz = Quiz(),
    val quizStat: QuizStat = QuizStat(),
)

@Immutable
data class QuizScreenData(
    val quizState: QuizState = QuizInit,
    val numberVocabularyNeed: Int = 0,
    val quizData: QuizData = QuizData(),
    val quizResult: QuizResultData? = null,
)