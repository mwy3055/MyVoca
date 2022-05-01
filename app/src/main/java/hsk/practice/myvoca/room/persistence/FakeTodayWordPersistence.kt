package hsk.practice.myvoca.room.persistence

import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.domain.TodayWordPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeTodayWordPersistence @Inject constructor() : TodayWordPersistence {

    private val fakeVocaPersistence = FakeVocaPersistence()

    private val todayWords = mutableListOf<TodayWord>()
    private val fakeTodayWordFlow = MutableStateFlow(todayWords)

    override fun loadTodayWords(): Flow<List<TodayWord>> {
        return fakeTodayWordFlow
    }

    override fun loadActualTodayWords(): Flow<List<Vocabulary>> {
        return flow {
            todayWords.map { todayWord ->
                fakeVocaPersistence.getVocabularyById(todayWord.wordId)
            }
        }
    }

    override suspend fun insertTodayWord(todayWord: TodayWord) {
        todayWords.add(todayWord)
    }

    override suspend fun insertTodayWords(todayWords: List<TodayWord>) {
        todayWords.forEach { insertTodayWord(it) }
    }

    override suspend fun updateTodayWord(todayWord: TodayWord) {
        val index = findIndex(todayWord)
        if (index == -1) {
            throw NoSuchElementException("$todayWord doesn't exist.")
        } else {
            todayWords[index] = todayWord
        }
    }

    override suspend fun deleteTodayWord(todayWord: TodayWord) {
        val index = findIndex(todayWord)
        if (index == -1) {
            throw NoSuchElementException("$todayWord doesn't exist.")
        } else {
            todayWords.removeAt(index)
        }
    }

    private fun findIndex(todayWord: TodayWord): Int {
        return todayWords.indexOfFirst { it.todayId == todayWord.todayId }
    }

    override suspend fun clearTodayWords() {
        todayWords.clear()
    }
}