package hsk.practice.myvoca.ui.screens.quiz

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.domain.VocaPersistence
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImplList
import hsk.practice.myvoca.util.MyVocaPreferences
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.util.randoms
import kotlinx.coroutines.flow.*
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

    private var initialLoad = true

    private val _quizScreenData = MutableStateFlow(QuizScreenData())
    val quizScreenData: StateFlow<QuizScreenData>
        get() = _quizScreenData

    private val resultDataFlow = MutableStateFlow<QuizResultData?>(null)

    init {
        viewModelScope.launch {
            combine(
                vocaPersistence.getAllVocabulary(),
                preferences.getPreferencesFlow(MyVocaPreferences.quizCorrectKey, 0),
                preferences.getPreferencesFlow(MyVocaPreferences.quizWrongKey, 0),
                resultDataFlow
            ) { allVocabulary, correct, wrong, quizResultData ->
                // Check if quiz data should be reloaded
                val value = quizScreenData.value
                if (!initialLoad and
                    (value.quizData.quizStat == QuizStat(correct, wrong)) and
                    (value.quizResult == quizResultData)
                ) {
                    // When only allVocabulary has new value, do not change any state
                    Logger.d("1")
                    return@combine quizScreenData.value.copy(numberVocabularyNeed = vocabularyRequired - allVocabulary.size)
                } else if (!initialLoad and
                    (value.quizData.quizStat == QuizStat(correct, wrong))
                ) {
                    // When quiz result is changed, return immediately
                    Logger.d("2")
                    return@combine quizScreenData.value.copy(quizResult = quizResultData)
                }
                // else: Loading start
                initialLoad = false
                _quizScreenData.value =
                    _quizScreenData.value.copy(quizState = QuizLoading())

                val quizState =
                    if (allVocabulary.size < vocabularyRequired) QuizNotAvailable() else QuizAvailable()
                val quizList = if (allVocabulary.size < vocabularyRequired) emptyList() else
                    allVocabulary.randoms(quizSize).toVocabularyImplList()
                val answerIndex = (0 until quizSize).random()

                // Loading complete
                QuizScreenData(
                    quizState = quizState,
                    numberVocabularyNeed = vocabularyRequired - allVocabulary.size,
                    quizData = QuizData(
                        quiz = Quiz(quizList, answerIndex),
                        quizStat = QuizStat(correct, wrong)
                    )
                )
            }.collect {
                _quizScreenData.value = it
//                Logger.d("Trying to emit new value: $it")
            }
        }
    }

    fun onQuizOptionSelected(index: Int) {
        Logger.d("$index item clicked!")
        val quiz = quizScreenData.value.quizData.quiz
        val result = if (index == quiz.answerIndex) QuizCorrect() else QuizWrong()
        resultDataFlow.value = QuizResultData(result, quiz.answer)
//        viewModelScope.launch {
//            resultDataFlow.emit(QuizResultData(result, quiz.answer))
//        }
    }

    fun onResultDialogClose(resultData: QuizResultData) {
        viewModelScope.launch {
            resultDataFlow.emit(null)

            val currentStat = quizScreenData.value.quizData.quizStat
            val (key, value) = if (resultData.result is QuizCorrect) {
                Pair(MyVocaPreferences.quizCorrectKey, currentStat.correct + 1)
            } else {
                Pair(MyVocaPreferences.quizWrongKey, currentStat.wrong + 1)
            }
            preferences.setPreferences(key, value)
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

class QuizLoading : QuizState()
class QuizAvailable : QuizState()
class QuizNotAvailable : QuizState()


/**
 * Result of the quiz.
 *
 * [QuizCorrect] indicates that the user chose the right answer.
 * [QuizWrong] indicates that the user failed the quiz.
 */
sealed class QuizResult

class QuizCorrect : QuizResult()
class QuizWrong : QuizResult()

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
    val quizState: QuizState = QuizLoading(),
    val numberVocabularyNeed: Int = 0,
    val quizData: QuizData = QuizData(),
    val quizResult: QuizResultData? = null,
)