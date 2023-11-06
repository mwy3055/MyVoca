package hsk.practice.myvoca.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.toTodayWord
import hsk.practice.myvoca.data.toTodayWordImpl
import hsk.practice.myvoca.module.ComputingDispatcher
import hsk.practice.myvoca.module.IoDispatcher
import hsk.practice.myvoca.module.LocalTodayWordPersistence
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImpl
import hsk.practice.myvoca.util.MyVocaPreferencesKey
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.work.setOneTimeTodayWordWork
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workManager: WorkManager,
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence,
    @LocalTodayWordPersistence private val todayWordPersistence: TodayWordPersistence,
    private val dataStore: PreferencesDataStore,
    @ComputingDispatcher private val computingDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _homeScreenData = MutableStateFlow(HomeScreenData(loading = true))
    val homeScreenData: StateFlow<HomeScreenData>
        get() = _homeScreenData

    init {
        loadScreenData()
    }

    private fun loadScreenData() {
        viewModelScope.launch(computingDispatcher) {
            combine(
                vocaPersistence.getVocabularySize(),
                todayWordPersistence.loadTodayWords(),
                todayWordPersistence.loadActualTodayWords(),
                dataStore.getPreferenceFlow(
                    MyVocaPreferencesKey.todayWordLastUpdatedKey,
                    LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC)
                )
            ) { size, todayWords, actualTodayWords, lastUpdated ->
                val data = homeScreenData.value
                _homeScreenData.value = homeScreenData.value.copy(loading = true)

                val todayWordList =
                    createHomeTodayWords(todayWords, actualTodayWords).sortTodayWords()
                        .toImmutableList()
                data.copy(
                    loading = false,
                    totalWordCount = size,
                    todayWords = todayWordList,
                    todayWordsLastUpdatedTime = lastUpdated
                )
            }.collect { _homeScreenData.value = it }
        }
    }

    private fun createHomeTodayWords(
        todayWords: List<TodayWord>,
        actualList: List<Vocabulary>
    ): List<HomeTodayWord> {
        Log.d("HomeViewModel", "$todayWords, $actualList")
        return todayWords.map { today ->
            val actual = actualList.find { it.id == today.wordId }
                ?: return@map null
            HomeTodayWord(today.toTodayWordImpl(), actual.toVocabularyImpl())
        }.mapNotNull { it }
    }

    // Click listeners for UI
    fun showTodayWordHelp(show: Boolean) {
        _homeScreenData.copyData(showTodayWordHelp = show)
    }

    fun onRefreshTodayWord() = viewModelScope.launch {
        setOneTimeTodayWordWork(workManager)
    }

    fun onTodayWordCheckboxChange(homeTodayWord: HomeTodayWord) {
        val checked = homeTodayWord.todayWord.checked
        val copy = homeTodayWord.todayWord.copy(checked = !checked)
        viewModelScope.launch(ioDispatcher) {
            todayWordPersistence.updateTodayWord(copy.toTodayWord())
        }
    }

    fun onCloseAlertDialog() {
        _homeScreenData.copyData(showTodayWordHelp = false)
    }

    private fun List<HomeTodayWord>.sortTodayWords(): List<HomeTodayWord> {
        return this.sortedBy { it.todayWord.id }
    }
}

private fun MutableStateFlow<HomeScreenData>.copyData(
    loading: Boolean = value.loading,
    totalWordCount: Int = value.totalWordCount,
    todayWords: ImmutableList<HomeTodayWord> = value.todayWords,
    todayWordsLastUpdatedTime: Long = value.todayWordsLastUpdatedTime,
    showTodayWordHelp: Boolean = value.showTodayWordHelp
) {
    synchronized(this) {
        value.copy(
            loading = loading,
            totalWordCount = totalWordCount,
            todayWords = todayWords,
            todayWordsLastUpdatedTime = todayWordsLastUpdatedTime,
            showTodayWordHelp = showTodayWordHelp
        ).also { value = it }
    }
}

data class HomeTodayWord(
    val todayWord: TodayWordImpl,
    val vocabulary: VocabularyImpl
)

data class HomeScreenData(
    val loading: Boolean = false,
    val totalWordCount: Int = 0,
    val todayWords: ImmutableList<HomeTodayWord> = persistentListOf(),
    val todayWordsLastUpdatedTime: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
    val showTodayWordHelp: Boolean = false
)

