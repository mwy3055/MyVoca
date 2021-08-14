package hsk.practice.myvoca.ui.screens.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWordViewModel @Inject constructor(
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence
) : ViewModel() {

    private val _addWordScreenData = MutableStateFlow(AddWordScreenData())
    val addWordScreenData: StateFlow<AddWordScreenData>
        get() = _addWordScreenData

    /* Event Listeners for UI */
    fun onWordUpdate(newWord: String) {
        _addWordScreenData.value = addWordScreenData.value.copy(word = newWord)
    }

    suspend fun loadStatus(newWord: String) {
        if (newWord.isEmpty()) {
            _addWordScreenData.value =
                addWordScreenData.value.copy(wordExistStatus = WordExistStatus.WORD_EMPTY)
            return
        }
        _addWordScreenData.value = addWordScreenData.value.copy(wordExistStatus = WordExistStatus.LOADING)
        val query = VocabularyQuery(word = newWord)
        val result = vocaPersistence.getVocabulary(query)
        val exists =
            if (result.any { it.eng == newWord }) WordExistStatus.DUPLICATE else WordExistStatus.NOT_EXISTS
        _addWordScreenData.value = addWordScreenData.value.copy(wordExistStatus = exists)
    }

    fun onMeaningAdd(type: WordClassImpl) {
        val newMeanings = addWordScreenData.value.meanings.toMutableList().apply {
            add(MeaningImpl(type, ""))
        }
        applyNewMeanings(newMeanings)
    }

    fun onMeaningUpdate(index: Int, meaning: MeaningImpl) {
        val newMeanings = addWordScreenData.value.meanings.toMutableList().apply {
            this[index] = meaning
        }
        applyNewMeanings(newMeanings)
    }

    fun onMeaningDelete(index: Int) {
        val newMeanings = addWordScreenData.value.meanings.toMutableList().apply {
            removeAt(index)
        }
        applyNewMeanings(newMeanings)
    }

    private fun applyNewMeanings(newMeanings: List<MeaningImpl>) {
        _addWordScreenData.value = addWordScreenData.value.copy(meanings = newMeanings)
    }

    fun onMemoUpdate(memo: String) {
        _addWordScreenData.value = addWordScreenData.value.copy(memo = memo)
    }

    fun onAddWord() {
        val word = addWordScreenData.value.toVocabularyImpl()
        viewModelScope.launch {
            vocaPersistence.insertVocabulary(listOf(word.toVocabulary()))
        }
    }

}

enum class WordExistStatus {
    NOT_EXISTS,
    DUPLICATE,
    LOADING,
    WORD_EMPTY
}

data class AddWordScreenData(
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