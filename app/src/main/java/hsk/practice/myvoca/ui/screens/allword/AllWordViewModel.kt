package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.domain.VocaPersistence
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.toVocabularyImplList
import hsk.practice.myvoca.ui.state.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllWordViewModel @Inject constructor(
    @LocalVocaPersistence val persistence: VocaPersistence
) : ViewModel() {

    private val _allWordUiState = MutableStateFlow(UiState<AllWordData>(loading = true))
    val allWordUiState: StateFlow<UiState<AllWordData>>
        get() = _allWordUiState

    val refreshChannel = Channel<Unit>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            refreshChannel.send(Unit)
        }
        lookRefreshChannel()
    }

    private suspend fun loadWords(query: VocabularyQuery): List<VocabularyImpl> {
        return persistence.getVocabulary(query).toVocabularyImplList()
    }

    private fun lookRefreshChannel() = viewModelScope.launch {
        for (refresh in refreshChannel) {
            Logger.d("Loading start, query: ${_allWordUiState.value.data?.queryState}")
            _allWordUiState.value = _allWordUiState.value.copy(loading = true)

            // TODO: remove this when test is finished
            delay(1500)

            val data = _allWordUiState.value.data ?: AllWordData()
            val result = loadWords(data.queryState)
            _allWordUiState.value = _allWordUiState.value.copy(
                loading = false,
                data = data.copy(currentWordState = result)
            )
            Logger.d("Loading complete!")
        }
    }

    /**
     * Event listeners for composable
     */
    fun onSearchOptionClicked() {
        _allWordUiState.value = _allWordUiState.value.copy(

        )
    }
}

@Immutable
data class AllWordData(
    val sortState: SortState = SortState.Alphabet,
    val queryState: VocabularyQuery = VocabularyQuery(),
    val currentWordState: List<VocabularyImpl> = emptyList()
)

enum class SortState { Alphabet, Latest, Random }