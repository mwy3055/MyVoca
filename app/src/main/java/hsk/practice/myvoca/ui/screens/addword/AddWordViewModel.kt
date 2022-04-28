package hsk.practice.myvoca.ui.screens.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabulary
import hsk.practice.myvoca.room.vocabulary.toVocabularyImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: 여기서부터 다시 리팩토링하기

@HiltViewModel
class AddWordViewModel @Inject constructor(
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(AddWordScreenData())
    val uiStateFlow: StateFlow<AddWordScreenData>
        get() = _uiStateFlow

    /**
     * 단어 수정 화면이라면 [injectUpdateWord] 함수를 이용하여 [currentVocabulary]를 초기화해야 한다.
     * 주의! [currentVocabulary]는 단 한 번만 초기화되어야 한다.
     */
    private var currentVocabulary: VocabularyImpl? = null

    fun injectUpdateWord(wordId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val word = getVocabulary(wordId)?.toVocabularyImpl()
            if (word != null) {
                currentVocabulary = word
                updateUiState(
                    screenType = UpdateWord,
                    word = word.eng,
                    meanings = word.meaning,
                    memo = word.memo ?: ""
                )
            }
        }
    }

    private suspend fun getVocabulary(id: Int): Vocabulary? = vocaPersistence.getVocabularyById(id)

    /* Event Listeners for UI */
    fun onWordUpdate(newWord: String) {
        updateUiState(word = newWord)
    }

    suspend fun loadStatus(newWord: String) {
        if (newWord.isEmpty()) {
            updateUiState(wordExistStatus = WordExistStatus.WORD_EMPTY)
            return
        }

        updateUiState(wordExistStatus = WordExistStatus.LOADING)

        val query = VocabularyQuery(word = newWord)
        val result = vocaPersistence.getVocabulary(query)

        val exists = if (currentVocabulary != null && newWord == currentVocabulary!!.eng) {
            WordExistStatus.NOT_EXISTS
        } else if (result.any { it.eng == newWord }) {
            WordExistStatus.DUPLICATE
        } else {
            WordExistStatus.NOT_EXISTS
        }
        updateUiState(wordExistStatus = exists)
    }

    fun onMeaningAdd(type: WordClassImpl) {
        val newMeanings = currentMeanings().apply {
            add(MeaningImpl(type, ""))
        }
        updateUiState(meanings = newMeanings)
    }

    fun onMeaningUpdate(index: Int, meaning: MeaningImpl) {
        val newMeanings = currentMeanings().apply {
            this[index] = meaning
        }
        updateUiState(meanings = newMeanings)
    }

    fun onMeaningDelete(index: Int) {
        val newMeanings = currentMeanings().apply {
            removeAt(index)
        }
        updateUiState(meanings = newMeanings)
    }

    private fun currentMeanings() = uiStateFlow.value.meanings.toMutableList()

    fun onMemoUpdate(memo: String) {
        updateUiState(memo = memo)
    }

    fun onAddWord() {
        if (currentVocabulary != null) {
            // 수정
            val updatedWord =
                uiStateFlow.value.toVocabularyImpl().copy(id = currentVocabulary!!.id)
            viewModelScope.launch(Dispatchers.IO) {
                vocaPersistence.updateVocabulary(listOf(updatedWord).map { it.toVocabulary() })
            }
        } else {
            // 추가
            val newWord = uiStateFlow.value.toVocabularyImpl()
            viewModelScope.launch(Dispatchers.IO) {
                vocaPersistence.insertVocabulary(listOf(newWord).map { it.toVocabulary() })
            }
        }
    }

    private fun updateUiState(
        screenType: ScreenType = uiStateFlow.value.screenType,
        word: String = uiStateFlow.value.word,
        wordExistStatus: WordExistStatus = uiStateFlow.value.wordExistStatus,
        meanings: List<MeaningImpl> = uiStateFlow.value.meanings,
        memo: String = uiStateFlow.value.memo
    ) {
        _uiStateFlow.value = AddWordScreenData(
            screenType = screenType,
            word = word,
            wordExistStatus = wordExistStatus,
            meanings = meanings,
            memo = memo
        )
    }

}

sealed class ScreenType

object AddWord : ScreenType()
object UpdateWord : ScreenType()

enum class WordExistStatus {
    NOT_EXISTS,
    DUPLICATE,
    LOADING,
    WORD_EMPTY
}

data class AddWordScreenData(
    val screenType: ScreenType = AddWord,
    val word: String = "",
    val wordExistStatus: WordExistStatus = WordExistStatus.WORD_EMPTY,
    val meanings: List<MeaningImpl> = emptyList(),
    val memo: String = "",
) {
    fun toVocabularyImpl(): VocabularyImpl {
        val current = System.currentTimeMillis()
        return VocabularyImpl(
            eng = word,
            meaning = meanings,
            addedTime = current,
            lastEditedTime = current,
            memo = memo
        )
    }

    val canStoreWord: Boolean
        get() = word.isNotEmpty() &&
                meanings.isNotEmpty() &&
                meanings.all { it.content.isNotEmpty() } &&
                wordExistStatus == WordExistStatus.NOT_EXISTS
}