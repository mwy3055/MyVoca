package hsk.practice.myvoca.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.hsk.data.TodayWord
import com.hsk.data.vocabulary.Vocabulary
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.toTodayWord
import hsk.practice.myvoca.data.toTodayWordImpl
import hsk.practice.myvoca.module.LocalTodayWordPersistence
import hsk.practice.myvoca.module.LocalVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImpl
import hsk.practice.myvoca.util.MyVocaPreferences
import hsk.practice.myvoca.util.PreferencesDataStore
import hsk.practice.myvoca.work.setOneTimeTodayWordWork
import hsk.practice.myvoca.work.setPeriodicTodayWordWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence,
    @LocalTodayWordPersistence private val todayWordPersistence: TodayWordPersistence,
    private val dataStore: PreferencesDataStore
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _homeScreenData = MutableStateFlow(HomeScreenData(loading = true))
    val homeScreenData: StateFlow<HomeScreenData>
        get() = _homeScreenData

    init {
        setPeriodicTodayWordWork(workManager)
        loadScreenData()
    }

    private fun loadScreenData() {
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                vocaPersistence.getVocabularySize(),
                todayWordPersistence.loadTodayWords(),
                todayWordPersistence.loadActualTodayWords(),
                dataStore.getPreferencesFlow(
                    MyVocaPreferences.todayWordLastUpdatedKey,
                    LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC)
                )
            ) { size, todayWords, actualTodayWords, lastUpdated ->
                val data = homeScreenData.value
                _homeScreenData.value = homeScreenData.value.copy(loading = true)

                val todayWordList =
                    createTodayWordList(todayWords, actualTodayWords).sortTodayWords()
                data.copy(
                    loading = false,
                    totalWordCount = size,
                    todayWords = todayWordList,
                    todayWordsLastUpdatedTime = lastUpdated
                )
            }.collect { _homeScreenData.value = it }
        }
    }

    private fun createTodayWordList(
        todayWords: List<TodayWord>,
        actualList: List<Vocabulary>
    ): List<HomeTodayWord> {
        return todayWords.map { today ->
            val actual = actualList.find { it.id == today.wordId } ?: return emptyList()
            HomeTodayWord(today.toTodayWordImpl(), actual.toVocabularyImpl())
        }
    }

    // Click listeners for Ui
    fun showTodayWordHelp(show: Boolean) {
        _homeScreenData.value = homeScreenData.value.copy(showTodayWordHelp = show)
    }

    fun onRefreshTodayWord() {
        viewModelScope.launch {
            setOneTimeTodayWordWork(workManager)
        }
    }

    fun onTodayWordCheckboxChange(homeTodayWord: HomeTodayWord) {
        val checked = homeTodayWord.todayWord.checked
        val copy = homeTodayWord.todayWord.copy(checked = !checked)
        viewModelScope.launch(Dispatchers.IO) {
            todayWordPersistence.updateTodayWord(copy.toTodayWord())
        }
    }

    fun onCloseAlertDialog() {
        _homeScreenData.value = homeScreenData.value.copy(showTodayWordHelp = false)
    }

    private fun List<HomeTodayWord>.sortTodayWords(): List<HomeTodayWord> {
        return this.sortedWith(compareBy({ it.todayWord.checked }, { it.todayWord.id }))
    }

}

data class HomeTodayWord(
    val todayWord: TodayWordImpl,
    val vocabulary: VocabularyImpl
)

data class HomeScreenData(
    val loading: Boolean = false,
    val totalWordCount: Int = 0,
    val todayWords: List<HomeTodayWord> = emptyList(),
    val todayWordsLastUpdatedTime: Long = System.currentTimeMillis(),
    val showTodayWordHelp: Boolean = false
)

/**
 * Calculates the remaining seconds of the day.
 */
fun getSecondsLeft(): Long {
    val time = LocalTime.now()
    return (60 * 60 * 24L) - (time.hour * 60 * 60) - (time.minute * 60) - (time.second)
}