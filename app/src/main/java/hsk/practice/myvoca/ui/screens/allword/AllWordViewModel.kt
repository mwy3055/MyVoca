package hsk.practice.myvoca.ui.screens.allword

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import com.hsk.ktx.xor
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.module.ComputingDispatcher
import hsk.practice.myvoca.module.IoDispatcher
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImplList
import hsk.practice.myvoca.room.vocabulary.toVocabularyList
import hsk.practice.myvoca.ui.state.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllWordViewModel @Inject constructor(
    @LocalVocaPersistence private val persistence: VocaPersistence,
    @ComputingDispatcher private val computingDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _allWordUiState = MutableStateFlow(UiState<AllWordData>(loading = true))
    val allWordUiState: StateFlow<UiState<AllWordData>>
        get() = _allWordUiState

    init {
        notifyWhenDatabaseUpdated()
    }

    private suspend fun loadWords(query: VocabularyQuery): List<VocabularyImpl> {
        return persistence.getVocabulary(query).toVocabularyImplList()
    }

    private fun notifyWhenDatabaseUpdated() {
        viewModelScope.launch(computingDispatcher) {
            persistence.getAllVocabulary().collectLatest {
                refreshWords()
            }
        }
    }

    private fun refreshWords() {
        viewModelScope.launch(ioDispatcher) {
            _allWordUiState.value = allWordUiState.value.copy(loading = true)

            val data = _allWordUiState.value.data ?: AllWordData()
            val result = loadWords(data.queryState).sortedBy(data.sortState)

            Log.d("AllWordViewModel", "$data, $result")
            _allWordUiState.value = allWordUiState.value.copy(
                loading = false,
                data = data.copy(currentWords = result.toImmutableList())
            )
        }
    }

    /**
     * Event listeners for composable
     */
    fun onSubmitButtonClicked() {
        refreshWords()
        _allWordUiState.copyData(submitState = true)
    }

    private fun onQueryChanged(query: VocabularyQuery) {
        val currentQuery = allWordUiState.value.data?.queryState
        if (currentQuery == null || currentQuery != query) {
            _allWordUiState.copyData(queryState = query)
        }
    }

    fun onQueryTextChanged(newQueryText: String) {
        val query = allWordUiState.value.data?.queryState ?: VocabularyQuery()
        onQueryChanged(query.copy(word = newQueryText))
    }

    fun onQueryWordClassToggled(koreanName: String) {
        val data = allWordUiState.value.data ?: AllWordData()
        data.queryState.let { queryState ->
            val current = queryState.wordClass
            val new = if (koreanName == totalWordClassName) {
                emptySet()
            } else {
                val wordClass = WordClassImpl.findByKorean(koreanName)?.toWordClass() ?: return
                current.xor(wordClass)
            }.toImmutableSet()
            onQueryChanged(queryState.copy(wordClass = new))
        }
    }

    fun onSortStateClicked(sortState: SortState) {
        val currentSortState = allWordUiState.value.data?.sortState
        if (currentSortState == null || currentSortState != sortState) {
            _allWordUiState.copyData(sortState = sortState)
        }
    }

    fun onClearOption() {
        _allWordUiState.copyData(
            sortState = SortState.defaultValue,
            queryState = VocabularyQuery(),
            submitState = false
        )
        refreshWords()
    }

    fun onWordUpdate(vocabularyImpl: VocabularyImpl?) {
        _allWordUiState.copyData(updateWord = vocabularyImpl)
    }

    fun onWordDelete(word: VocabularyImpl) = viewModelScope.launch(ioDispatcher) {
        persistence.deleteVocabulary(listOf(word).toVocabularyList())
        _allWordUiState.copyData(deletedWord = word)
        onWordDeleteCompleteUpdate(flag = true)
    }

    fun onWordDeleteCompleteUpdate(flag: Boolean) {
        _allWordUiState.copyData(deleteWordComplete = flag)
    }

    fun onWordRestore(word: VocabularyImpl) = viewModelScope.launch(ioDispatcher) {
        persistence.insertVocabulary(listOf(word).toVocabularyList())
        _allWordUiState.copyData(deletedWord = null)
    }
}

private fun Collection<VocabularyImpl>.sortedBy(selector: SortState): List<VocabularyImpl> {
    return when (selector) {
        SortState.Alphabet -> {
            this.sortedBy { it.eng }
        }

        SortState.Latest -> {
            this.sortedByDescending { it.addedTime }
        }

        SortState.Random -> {
            this.shuffled()
        }
    }
}

private fun MutableStateFlow<UiState<AllWordData>>.copyData(
    sortState: SortState? = null,
    queryState: VocabularyQuery? = null,
    currentWords: List<VocabularyImpl>? = null,
    submitState: Boolean? = null,
    updateWord: VocabularyImpl? = null,
    deletedWord: VocabularyImpl? = null,
    deleteWordComplete: Boolean? = null
) {
    synchronized(this) {
        val data = value.data ?: AllWordData()
        val newData = data.copy(
            sortState = sortState ?: data.sortState,
            queryState = queryState ?: data.queryState,
            submitState = submitState ?: data.submitState,
            currentWords = currentWords?.toImmutableList() ?: data.currentWords,
            updateWord = updateWord ?: data.updateWord,
            deletedWord = deletedWord ?: data.deletedWord,
            deleteWordComplete = deleteWordComplete ?: data.deleteWordComplete
        )
        this.value = this.value.copy(data = newData)
    }
}

data class AllWordData(
    val sortState: SortState = SortState.defaultValue,
    val queryState: VocabularyQuery = VocabularyQuery(),
    val submitState: Boolean = false,
    val currentWords: ImmutableList<VocabularyImpl> = persistentListOf(),
    val updateWord: VocabularyImpl? = null,
    val deletedWord: VocabularyImpl? = null,
    val deleteWordComplete: Boolean = false,
)

enum class SortState(val korean: String) {
    Alphabet("알파벳"),
    Latest("최신순"),
    Random("무작위");

    companion object {
        val defaultValue: SortState = Alphabet
    }
}

internal const val totalWordClassName = "전체"