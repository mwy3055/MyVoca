package hsk.practice.myvoca.room.persistence

import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeTodayWordPersistence @Inject constructor(
    private val vocaPersistence: VocaPersistence
) : TodayWordPersistence {

    private val todayWordsFlow = MutableStateFlow(persistentListOf<TodayWord>())
    private val todayWords: ImmutableList<TodayWord>
        get() = todayWordsFlow.value.toImmutableList()

    override fun loadTodayWords(): Flow<List<TodayWord>> = todayWordsFlow

    override fun loadActualTodayWords(): Flow<List<Vocabulary>> = flow {
        todayWords.map { vocaPersistence.getVocabularyById(it.wordId) }
    }

    override suspend fun insertTodayWord(newTodayWord: TodayWord) {
        updateValue {
            add(newTodayWord)
        }
    }

    override suspend fun insertTodayWords(newTodayWords: List<TodayWord>) {
        updateValue {
            addAll(newTodayWords)
        }
    }

    override suspend fun updateTodayWord(todayWord: TodayWord) =
        onlyWhenExists(todayWord) { target ->
            updateValue {
                removeAll { it.todayId == target.todayId }
                add(target)
            }
        }

    override suspend fun deleteTodayWord(todayWord: TodayWord) =
        onlyWhenExists(todayWord) { target ->
            updateValue {
                remove(target)
            }
        }

    override suspend fun clearTodayWords() {
        updateValue {
            clear()
        }
    }

    private suspend fun onlyWhenExists(todayWord: TodayWord, block: suspend (TodayWord) -> Unit) {
        if (todayWords.any { it.todayId == todayWord.todayId }) {
            block(todayWord)
        } else {
            throw NoSuchElementException("$todayWord doesn't exist. Exists: $todayWords")
        }
    }

    /**
     * All functions which desire to modify flow's value should only use this function.
     * Any other changes are invalid.
     */
    private fun updateValue(action: MutableList<TodayWord>.() -> Unit) {
        val newValue = todayWordsFlow.value.mutate { action(it) }
        todayWordsFlow.value = newValue
    }
}