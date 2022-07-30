package hsk.practice.myvoca.room.persistence

import com.hsk.data.TodayWord
import com.hsk.data.Vocabulary
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeTodayWordPersistence @Inject constructor(
    private val vocaPersistence: VocaPersistence
) : TodayWordPersistence {

    private val todayWords = mutableSetOf<TodayWord>()

    override fun loadTodayWords(): Flow<List<TodayWord>> = flow {
        emit(todayWords.toList())
    }

    override fun loadActualTodayWords(): Flow<List<Vocabulary>> = flow {
        todayWords.map { vocaPersistence.getVocabularyById(it.wordId) }
    }

    override suspend fun insertTodayWord(newTodayWord: TodayWord) {
        todayWords.add(newTodayWord)
    }

    override suspend fun insertTodayWords(newTodayWords: List<TodayWord>) {
        todayWords.addAll(newTodayWords)
    }

    override suspend fun updateTodayWord(todayWord: TodayWord) =
        onlyWhenExists(todayWord) { target ->
            todayWords.removeAll { it.todayId == target.todayId }
            todayWords.add(target)
        }

    override suspend fun deleteTodayWord(todayWord: TodayWord) =
        onlyWhenExists(todayWord) { target ->
            todayWords.remove(target)
        }

    override suspend fun clearTodayWords() {
        todayWords.clear()
    }

    private fun onlyWhenExists(todayWord: TodayWord, block: (TodayWord) -> Unit) {
        if (todayWords.any { it.todayId == todayWord.todayId }) {
            block(todayWord)
        } else {
            throw NoSuchElementException("$todayWord doesn't exist. Exists: $todayWords")
        }
    }
}