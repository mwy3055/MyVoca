package hsk.practice.myvoca.ui.quiz

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.VocaRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.dataStore
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyList
import hsk.practice.myvoca.module.RoomVocaRepository
import hsk.practice.myvoca.ui.customview.VersusViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val quizCorrectKey = "quiz_correct"
private val QUIZ_CORRECT_KEY = intPreferencesKey(quizCorrectKey)

private const val quizWrongKey = "quiz_wrong"
private val QUIZ_WRONG_KEY = intPreferencesKey(quizWrongKey)

fun getIntFlow(context: Context, key: Preferences.Key<Int>): Flow<Int> =
    context.dataStore.data.map { preferences ->
        preferences[key] ?: 0
    }

fun ViewModel.setIntValue(context: Context, key: Preferences.Key<Int>, value: Int) =
    viewModelScope.launch {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
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

    fun loadValues(context: Context) = viewModelScope.launch {
        getIntFlow(context, QUIZ_CORRECT_KEY).take(1).collectLatest { correct ->
            versusViewModel.setLeftValue(correct)
        }
        getIntFlow(context, QUIZ_WRONG_KEY).take(1).collectLatest { wrong ->
            versusViewModel.setRightValue(wrong)
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

    fun quizItemSelected(context: Context, index: Int) {
        val result = (index == quizData.value!!.answerIndex)
        if (result) {
            onAnswer(context)
        } else {
            onWrong(context)
        }

        showVocaDialog(context, quizData.value!!.answer, result)
        prepareQuiz()
    }

    private fun onAnswer(context: Context) {
        versusViewModel.increaseLeftValue()
        setIntValue(context, QUIZ_CORRECT_KEY, versusViewModel.leftValue.value!!)
    }

    private fun onWrong(context: Context) {
        versusViewModel.increaseRightValue()
        setIntValue(context, QUIZ_WRONG_KEY, versusViewModel.rightValue.value!!)
    }

    private fun showVocaDialog(context: Context, voca: RoomVocabulary, isCorrect: Boolean) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(if (isCorrect) "맞았습니다!!" else "틀렸습니다")
        builder.setMessage(formatAnswer(voca))
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.show()
    }

    // replace new line character to the space
    private fun formatAnswer(voca: RoomVocabulary) =
        "${voca.eng}: ${voca.kor?.replace("\n", " ")}"

}