package hsk.practice.myvoca.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyList
import hsk.practice.myvoca.module.RoomVocaRepository
import hsk.practice.myvoca.ui.customview.VersusViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizResult(val answer: RoomVocabulary) {
    class QuizCorrect(answer: RoomVocabulary) : QuizResult(answer)
    class QuizWrong(answer: RoomVocabulary) : QuizResult(answer)
}

/**
 * [ViewModel] for QuizFragment.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(@RoomVocaRepository private val vocaRepository: VocaRepository) :
    ViewModel() {

    private lateinit var allVocabularyFlow: StateFlow<List<RoomVocabulary?>?>

    private lateinit var quizAvailable: StateFlow<Boolean>

    lateinit var versusViewModel: VersusViewModel

    private val _quizData = MutableLiveData<Quiz?>()
    val quizData: LiveData<Quiz?>
        get() = _quizData

    private val _quizResult = MutableLiveData<QuizResult?>()
    val quizResult: LiveData<QuizResult?>
        get() = _quizResult

    init {
        viewModelScope.launch {
            allVocabularyFlow = getAllVocabulary()
            quizAvailable = getQuizAvailable(allVocabularyFlow)
            quizAvailable.take(2).collect {
                prepareQuiz()
            }
        }
    }

    private suspend fun getAllVocabulary() = vocaRepository.getAllVocabulary().map {
        Logger.d("QuizViewModel.allVocabularyFlow set!")
        it.toRoomVocabularyList()
    }.stateIn(viewModelScope)

    private suspend fun getQuizAvailable(allVocaFlow: StateFlow<List<RoomVocabulary?>?>) =
        allVocaFlow.map {
            it?.let { it.size >= 4 } == true
        }.stateIn(viewModelScope)

    private fun prepareQuiz() = viewModelScope.launch {
        Logger.d("Quiz available? ${quizAvailable.value}")
        if (quizAvailable.value) {
            val quizList = loadQuizVocabulary(allVocabularyFlow.value!!)
            val answerIndex = quizList.indices.random()
            _quizData.value = Quiz(quizList, answerIndex)
        } else {
            clearQuiz()
        }
    }

    /**
     * Load quiz options. Size of [allVoca] should be at least 4.
     *
     * @param allVoca List of all vocabularies. Length should be at least 4.
     * @return list of quiz items
     */
    private fun loadQuizVocabulary(allVoca: List<RoomVocabulary?>): List<RoomVocabulary> {
        val vocaNotNullSet = mutableSetOf<RoomVocabulary>()
        while (vocaNotNullSet.size < 4) {
            vocaNotNullSet.add(allVoca.random() ?: continue)
        }
        return vocaNotNullSet.toList()
    }

    private fun clearQuiz() {
        _quizData.value = null
    }

    fun quizItemSelected(index: Int) {
        val result = (index == quizData.value!!.answerIndex)
        val answer = quizData.value!!.answer
        _quizResult.value = if (result) {
            QuizResult.QuizCorrect(answer)
        } else {
            QuizResult.QuizWrong(answer)
        }

        prepareQuiz()
    }

    fun quizResultComplete() {
        _quizResult.value = null
    }

}