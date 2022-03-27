package hsk.practice.myvoca.room.persistence

import com.hsk.data.vocabulary.*
import com.hsk.domain.VocaPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class FakeVocaPersistence @Inject constructor() : VocaPersistence, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val current = System.currentTimeMillis()

    private val data = mutableListOf(
        Vocabulary(1, "apple", listOf(Meaning(WordClass.NOUN, "사과")), current, current, ""),
        Vocabulary(2, "banana", listOf(Meaning(WordClass.NOUN, "바나나")), current, current, "")
    )
    private val fakeDataFlow = MutableStateFlow<List<Vocabulary>>(data)

    override fun getAllVocabulary(): StateFlow<List<Vocabulary>> = fakeDataFlow
    override fun getVocabularySize(): Flow<Int> = fakeDataFlow.map { it.size }

    override suspend fun getVocabularyById(id: Int): Vocabulary {
        return try {
            data.first { it.id == id }
        } catch (e: NoSuchElementException) {
            nullVocabulary
        }
    }

    override suspend fun getVocabulary(query: VocabularyQuery): List<Vocabulary> {
        return data.filter { vocabulary -> vocabulary.matchesWithQuery(query) }
    }

    override suspend fun insertVocabulary(vocabularies: List<Vocabulary>) {
        data.addAll(vocabularies)
        data.sortBy { it.eng }
    }

    override suspend fun updateVocabulary(vocabularies: List<Vocabulary>) {
        // Don't have to be implemented
    }

    override suspend fun deleteVocabulary(vocabularies: List<Vocabulary>) {
        data.removeAll(vocabularies)
    }
}