package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabulary
import hsk.practice.myvoca.room.vocabulary.toVocabularyImplList
import hsk.practice.myvoca.ui.state.UiState
import hsk.practice.myvoca.util.xor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllWordViewModel @Inject constructor(
    @LocalVocaPersistence val persistence: VocaPersistence
) : ViewModel() {

    private val _allWordUiState = MutableStateFlow(UiState<AllWordData>(loading = true))
    val allWordUiState: StateFlow<UiState<AllWordData>>
        get() = _allWordUiState

    private val refreshChannel = Channel<Unit>(Channel.CONFLATED)

    init {
        notifyWhenDatabaseUpdated()
        lookRefreshChannel()
    }

    private suspend fun loadWords(query: VocabularyQuery): List<VocabularyImpl> {
        return persistence.getVocabulary(query).toVocabularyImplList()
    }

    private fun notifyRefresh() = viewModelScope.launch(Dispatchers.Default) {
        refreshChannel.send(Unit)
    }

    private fun notifyWhenDatabaseUpdated() {
        viewModelScope.launch {
            persistence.getAllVocabulary().collectLatest {
                notifyRefresh()
            }
        }
    }

    private fun lookRefreshChannel() = viewModelScope.launch(Dispatchers.Default) {
        for (refresh in refreshChannel) {
            _allWordUiState.value = allWordUiState.value.copy(loading = true)
//            Logger.d("Loading start, data: ${allWordUiState.value}")

            val data = _allWordUiState.value.data ?: AllWordData()
            val result = loadWords(data.queryState).sortedBy(data.sortState)
            _allWordUiState.value = allWordUiState.value.copy(
                loading = false,
                data = data.copy(currentWordState = result)
            )
//            Logger.d("Loading complete!")
        }
    }

    /**
     * Event listeners for composable
     */
    fun onSubmitButtonClicked() {
        notifyRefresh()
    }

    private fun onQueryChanged(query: VocabularyQuery) {
        val data = allWordUiState.value.data ?: return
        val originalQuery = data.queryState
        if (originalQuery != query) {
            _allWordUiState.copyData(queryState = query)
        }
    }

    fun onQueryTextChanged(value: String) {
        allWordUiState.value.data?.queryState?.let {
            onQueryChanged(it.copy(word = value))
        }
    }

    fun onQueryWordClassToggled(koreanName: String) {
        allWordUiState.value.data?.queryState?.let { queryState ->
            val current = queryState.wordClass
            val new = if (koreanName == totalWordClassName) {
                emptySet()
            } else {
                val wordClass = WordClassImpl.findByKorean(koreanName)?.toWordClass() ?: return
                current.xor(wordClass).toSet()
            }
            onQueryChanged(queryState.copy(wordClass = new))
        }
    }

    fun onSortStateClicked(sortState: SortState) {
        allWordUiState.value.data?.let { data ->
            val currentSortState = data.sortState
            if (sortState != currentSortState) {
                _allWordUiState.copyData(sortState = sortState)
                notifyRefresh()
            }
        }
    }

    fun onClearOption() {
        _allWordUiState.copyData(queryState = VocabularyQuery())
        notifyRefresh()
    }

    fun onWordDelete(word: VocabularyImpl) {
        viewModelScope.launch {
            persistence.deleteVocabulary(listOf(word.toVocabulary()))
            _allWordUiState.copyData(deletedWord = word)
            delay(100L)
            _allWordUiState.copyData(deletedWord = null)
        }
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
    currentWordState: List<VocabularyImpl>? = null,
    deletedWord: VocabularyImpl? = null
) {
    synchronized(this) {
        val data = value.data ?: return
        val newData = data.copy(
            sortState = sortState ?: data.sortState,
            queryState = queryState ?: data.queryState,
            currentWordState = currentWordState ?: data.currentWordState,
            deletedWord = deletedWord
        )
        this.value = this.value.copy(data = newData)
    }
}

@Immutable
data class AllWordData(
    val sortState: SortState = SortState.Alphabet,
    val queryState: VocabularyQuery = VocabularyQuery(),
    val currentWordState: List<VocabularyImpl> = emptyList(),
    val deletedWord: VocabularyImpl? = null
)

enum class SortState(val korean: String) {
    Alphabet("알파벳"),
    Latest("최신순"),
    Random("무작위")
}

const val totalWordClassName = "전체"