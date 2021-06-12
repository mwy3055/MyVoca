package hsk.practice.myvoca.framework

import com.hsk.data.VocaPersistence
import com.hsk.domain.vocabulary.Vocabulary
import hsk.practice.myvoca.containsOnlyAlphabet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class FakeVocaPersistence @Inject constructor() : VocaPersistence, CoroutineScope {

    val current = System.currentTimeMillis()

    val data = mutableListOf(
        Vocabulary(1, "apple", "사과", current, current, ""),
        Vocabulary(2, "banana", "바나나", current, current, "")
    )

    private val fakeDataFlow = MutableStateFlow<List<Vocabulary?>>(data)

    override fun getAllVocabulary(): StateFlow<List<Vocabulary?>> = fakeDataFlow

    override suspend fun getVocabulary(query: String): List<Vocabulary?> {
        return if (query.containsOnlyAlphabet()) {
            data.filter { it.eng.contains(query) }
        } else {
            data.filter { it.kor?.contains(query) ?: false }
        }
    }

    override suspend fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        for (voca in vocabularies) {
            voca?.let { data.add(it) }
        }
        data.sortBy { it.eng }
    }

    override suspend fun updateVocabulary(vararg vocabularies: Vocabulary?) {
        // Not necessary, so didn't implemented.
    }

    override suspend fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        for (voca in vocabularies) {
            data.remove(voca)
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}