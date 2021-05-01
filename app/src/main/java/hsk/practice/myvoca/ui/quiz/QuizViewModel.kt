package hsk.practice.myvoca.ui.quiz

import android.content.Context
import androidx.lifecycle.*
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.PreferenceManager
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyList
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for QuizFragment.
 * <b>
 *
 * Among many LiveData related to quiz, QuizFragment should only observe quizVocabulary.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(@RoomVocaRepository private val vocaRepository: VocaRepository) : ViewModel() {

    private val _allVocabulary = MutableLiveData<List<RoomVocabulary?>?>()
    private val allVocabulary: LiveData<List<RoomVocabulary?>?>
        get() = _allVocabulary

    init {
        loadAllVocabulary()
    }

    private fun loadAllVocabulary() = viewModelScope.launch {
        val allVocabularyFlow = vocaRepository.getAllVocabulary()
        _allVocabulary.value = allVocabularyFlow.value.toRoomVocabularyList()
        allVocabularyFlow.collect {
            _allVocabulary.value = it.toRoomVocabularyList()
        }
    }

    /**
     * Mapped object for initializing LiveData related to quiz.
     * For observe only. Observers should not execute any code.
     */
    val quizAvailable: LiveData<Boolean> = Transformations.map(allVocabulary) {
        Logger.d("Quiz initialized.. ${it == null}")
        val result = if (it != null) it.size >= 4 else false
        val loadResult = viewModelScope.async {
            if (result) {
                prepareQuiz(it!!)
            } else {
                clearQuiz()
            }
        }
        viewModelScope.launch {
            setQuizPreparedEvent(loadResult.await())
        }
        result
    }

    private val _quizVocabulary = MutableLiveData<List<RoomVocabulary>>()
    val quizVocabulary: LiveData<List<RoomVocabulary>>
        get() = _quizVocabulary

    private var answerIndex = 0

    /**
     * This LiveData should be only observed at QuizFragment.
     */
    private val _answerVoca = MutableLiveData<RoomVocabulary?>()
    val answerVoca: LiveData<RoomVocabulary?>
        get() = _answerVoca

    private val _quizLoadCompleteEvent = MutableLiveData<Boolean?>()
    val quizLoadCompleteEvent: LiveData<Boolean?>
        get() = _quizLoadCompleteEvent

    private val _answerEvent = MutableLiveData<Boolean?>()
    val answerEvent: LiveData<Boolean?>
        get() = _answerEvent

    private var _answerCount = 0
    val answerCount: Int
        get() = _answerCount

    private var _wrongCount = 0
    val wrongCount: Int
        get() = _wrongCount

    fun loadPreferences(context: Context) {
        _answerCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_CORRECT)
        _wrongCount = PreferenceManager.getInt(context, PreferenceManager.QUIZ_WRONG)
    }

    private fun loadQuizVocabulary(allVoca: List<RoomVocabulary?>): List<RoomVocabulary> {
        val vocaNotNullSet = mutableSetOf<RoomVocabulary>()
        while (vocaNotNullSet.size < 4) {
            vocaNotNullSet.add(allVoca.random() ?: continue)
        }
        return vocaNotNullSet.toList()
    }

    /**
     * Load quiz options, choose answer index and vocabulary
     */
    private fun prepareQuiz(allVoca: List<RoomVocabulary?>): Boolean {
        return if (allVoca.size >= 4) {
            val quizList = loadQuizVocabulary(allVoca)
            answerIndex = quizList.indices.random()
            _quizVocabulary.value = quizList.toMutableList()
            _answerVoca.value = quizList[answerIndex]
            true
        } else {
            clearQuiz()
        }
    }

    private fun clearQuiz(): Boolean {
        _answerVoca.value = null
        clearQuizPreparedEvent()
        return false
    }

    private fun setQuizPreparedEvent(value: Boolean) {
        _quizLoadCompleteEvent.value = value
    }

    fun clearQuizPreparedEvent() {
        _quizLoadCompleteEvent.value = null
    }

    fun quizItemSelected(context: Context, index: Int) {
        answerVoca.value?.let {
            val correct = (index == answerIndex)
            if (correct) {
                _answerCount++
                PreferenceManager.setInt(context, PreferenceManager.QUIZ_CORRECT, answerCount)
            } else {
                _wrongCount++
                PreferenceManager.setInt(context, PreferenceManager.QUIZ_WRONG, wrongCount)
            }
            setAnswerEvent(correct)
            viewModelScope.launch {
                val prepareResult = allVocabulary.value?.let { prepareQuiz(it) } ?: false
                setQuizPreparedEvent(prepareResult)
            }
        }
    }

    private fun setAnswerEvent(value: Boolean) {
        _answerEvent.value = value
    }

    fun clearAnswerEvent() {
        _answerEvent.value = null
    }

}