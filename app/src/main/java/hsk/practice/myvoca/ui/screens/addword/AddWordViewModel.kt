package hsk.practice.myvoca.ui.screens.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.WebViewState
import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImpl
import hsk.practice.myvoca.room.vocabulary.toVocabularyList
import hsk.practice.myvoca.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWordViewModel @Inject constructor(
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(AddWordScreenData())
    val uiStateFlow: StateFlow<AddWordScreenData>
        get() = _uiStateFlow

    val webViewState = WebViewState(WebContent.Url(url = ""))
    val webViewNavigator = WebViewNavigator(viewModelScope)

    /**
     * 단어 수정 화면이라면 [injectUpdateTarget] 함수를 이용하여 [updateTarget]를 초기화해야 한다.
     * 주의! [updateTarget]은 단 한 번만 초기화되어야 한다.
     */
    private var updateTarget: VocabularyImpl? = null

    fun injectUpdateTarget(wordId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val word = getVocabulary(wordId)?.toVocabularyImpl()
            if (word != null) {
                updateTargetWord(word)
            }
        }
    }

    private fun updateTargetWord(word: VocabularyImpl) {
        val meaningExistStatuses = mutableListOf<MeaningExistStatus>()
        for (i in 1..word.meaning.size) {
            meaningExistStatuses.add(
                MeaningExistStatus.NOT_EXISTS
            )
        }
        updateTarget = word
        updateUiState(
            screenType = UpdateWord,
            word = word.eng,
            wordExistStatus = WordExistStatus.NOT_EXISTS,
            meanings = word.meaning,
            meaningExistStatuses = meaningExistStatuses.toImmutableList(),
            memo = word.memo ?: ""
        )
    }

    private suspend fun getVocabulary(id: Int): Vocabulary? = vocaPersistence.getVocabularyById(id)

    /* Event Listeners for UI */
    fun onWordUpdate(newWord: String) {
        updateUiState(word = newWord)
    }

    fun onWordClear() {
        updateUiState(word = "")
    }

    suspend fun loadStatus(newWord: String) {
        if (newWord.isEmpty()) {
            updateUiState(wordExistStatus = WordExistStatus.WORD_EMPTY)
            return
        }

        updateUiState(wordExistStatus = WordExistStatus.LOADING)

        val query = VocabularyQuery(word = newWord)
        val result = vocaPersistence.getVocabulary(query)

        val exists = if (updateTarget == null && result.any { it.eng == newWord }) {
            WordExistStatus.DUPLICATE
        } else {
            WordExistStatus.NOT_EXISTS
        }
        updateUiState(wordExistStatus = exists)
    }

    fun onMeaningCheck(index: Int, newMeaning: MeaningImpl) {
        val currentMeanings = uiStateFlow.value.meanings
        val currentStatusList = applyToMeaningStatuses {
            if (index != -1) {
                if (newMeaning.content.isEmpty()) {
                    this[index] = MeaningExistStatus.MEANING_EMPTY
                } else {
                    val exists = if (currentMeanings.subList(0, index).contains(newMeaning)) {
                        MeaningExistStatus.DUPLICATE
                    } else {
                        MeaningExistStatus.NOT_EXISTS
                    }
                    this[index] = exists
                }
            }
        }
        updateUiState(meaningExistStatuses = currentStatusList)
    }

    fun onMeaningAdd(type: WordClassImpl) {
        val newMeanings = applyToMeanings {
            add(MeaningImpl(type, ""))
        }
        val newStatusList = applyToMeaningStatuses {
            add(MeaningExistStatus.NOT_EXISTS)
        }
        updateUiState(
            meanings = newMeanings,
            meaningExistStatuses = newStatusList
        )
    }

    fun onMeaningUpdate(index: Int, meaning: MeaningImpl) {
        val newMeanings = applyToMeanings {
            this[index] = meaning
        }
        val newStatusList = applyToMeaningStatuses {
            this[index] = if (meaning.content.isEmpty()) MeaningExistStatus.MEANING_EMPTY
            else MeaningExistStatus.NOT_EXISTS
        }

        updateUiState(
            meanings = newMeanings,
            meaningExistStatuses = newStatusList
        )
    }

    fun onMeaningDelete(index: Int) {
        val newMeanings = applyToMeanings {
            removeAt(index)
        }
        val newStatuses = applyToMeaningStatuses {
            removeAt(index)
        }
        updateUiState(
            meanings = newMeanings,
            meaningExistStatuses = newStatuses
        )
    }

    private fun applyToMeanings(block: MutableList<MeaningImpl>.() -> Unit): ImmutableList<MeaningImpl> {
        val meanings = uiStateFlow.value.meanings.toMutableList()
        return meanings.apply { block() }.toImmutableList()
    }

    private fun applyToMeaningStatuses(block: MutableList<MeaningExistStatus>.() -> Unit): ImmutableList<MeaningExistStatus> {
        val meaningExistStatuses = uiStateFlow.value.meaningExistStatuses.toMutableList()
        return meaningExistStatuses.apply { block() }.toImmutableList()
    }

    fun onMemoUpdate(memo: String) {
        updateUiState(memo = memo)
    }

    fun onAddWord() {
        if (updateTarget == null) {
            insertVocabulary()
        } else {
            updateVocabulary()
        }
    }

    private fun insertVocabulary() {
        val newWord = uiStateFlow.value.toVocabularyImpl()
        viewModelScope.launch(Dispatchers.IO) {
            vocaPersistence.insertVocabulary(listOf(newWord).toVocabularyList())
        }
    }

    private fun updateVocabulary() {
        val updatedWord = uiStateFlow.value.toVocabularyImpl().copy(id = updateTarget!!.id)
        viewModelScope.launch(Dispatchers.IO) {
            vocaPersistence.updateVocabulary(listOf(updatedWord).toVocabularyList())
        }
    }

    fun onShowWebView() {
        if (uiStateFlow.value.word.isNotEmpty()) {
            updateUiState(showWebView = true)
        }
    }

    fun onHideWebView() {
        updateUiState(showWebView = false)
    }

    fun onUpdateWebViewUrl() {
        updateUiState(
            webViewUrl = UiText.StringResource(
                resId = R.string.web_view_url,
                uiStateFlow.value.word
            )
        )
    }

    private fun updateUiState(
        screenType: ScreenType = uiStateFlow.value.screenType,
        word: String = uiStateFlow.value.word,
        wordExistStatus: WordExistStatus = uiStateFlow.value.wordExistStatus,
        meanings: ImmutableList<MeaningImpl> = uiStateFlow.value.meanings,
        meaningExistStatuses: ImmutableList<MeaningExistStatus> = uiStateFlow.value.meaningExistStatuses,
        memo: String = uiStateFlow.value.memo,
        showWebView: Boolean = uiStateFlow.value.showWebView,
        webViewUrl: UiText = uiStateFlow.value.webViewUrl
    ) {
        _uiStateFlow.value = AddWordScreenData(
            screenType = screenType,
            word = word,
            wordExistStatus = wordExistStatus,
            meanings = meanings,
            meaningExistStatuses = meaningExistStatuses,
            memo = memo,
            showWebView = showWebView,
            webViewUrl = webViewUrl
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

enum class MeaningExistStatus {
    NOT_EXISTS,
    DUPLICATE,
    LOADING,
    MEANING_EMPTY
}

data class AddWordScreenData(
    val screenType: ScreenType = AddWord,
    val word: String = "",
    val wordExistStatus: WordExistStatus = WordExistStatus.WORD_EMPTY,
    val meanings: ImmutableList<MeaningImpl> = persistentListOf(),
    val meaningExistStatuses: ImmutableList<MeaningExistStatus> = persistentListOf(),
    val memo: String = "",
    val showWebView: Boolean = false,
    val webViewUrl: UiText = UiText.DirectString(""),
) {
    fun toVocabularyImpl(): VocabularyImpl {
        val current = System.currentTimeMillis()
        return VocabularyImpl(
            eng = word,
            meaning = meanings.toImmutableList(),
            addedTime = current,
            lastEditedTime = current,
            memo = memo
        )
    }

    val canStoreWord: Boolean
        get() = word.isNotEmpty() &&
                meanings.isNotEmpty() &&
                meanings.all { it.content.isNotEmpty() } &&
                wordExistStatus == WordExistStatus.NOT_EXISTS &&
                meaningExistStatuses.all { it == MeaningExistStatus.NOT_EXISTS }
}