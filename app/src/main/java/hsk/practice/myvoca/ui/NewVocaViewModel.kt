package hsk.practice.myvoca.ui

import androidx.lifecycle.ViewModel
import com.hsk.data.VocaPersistence
import com.hsk.data.VocaRepository
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabularyList
import hsk.practice.myvoca.framework.toVocabularyArray

/**
 * VocaViewModel is at the top of the database abstraction.
 * ViewModel interacts with the UI classes directly.
 * All database operations must be done through this class.
 *
 * Also, all methods work asynchronously because database access is costly.
 * Methods return the LiveData immediately when the method is called. Actual result will be filled into LiveData later.
 * UI classes should observe the LiveData and define what to do when the operation is actually finished.
 */
class NewVocaViewModel(vocaPersistence: VocaPersistence) : ViewModel() {

    private var vocaRepository: VocaRepository = VocaRepository(vocaPersistence)

    private var allVocabulary: List<RoomVocabulary?>?

    init {
        allVocabulary = loadVocabulary()
    }

    @Synchronized
    private fun loadVocabulary() = vocaRepository.getAllVocabulary().toRoomVocabularyList()

    fun getAllVocabulary(): List<RoomVocabulary?> {
        if (allVocabulary == null) {
            allVocabulary = loadVocabulary()
        }
        return allVocabulary!!
    }

    fun getVocabularyCount(): Int = getAllVocabulary().size

    fun deleteVocabulary(vararg vocabularies: RoomVocabulary) {
        vocaRepository.deleteVocabulary(*vocabularies.toVocabularyArray())
    }

    fun updateVocabulary(vararg vocabularies: RoomVocabulary) {
        vocaRepository.updateVocabulary(*vocabularies.toVocabularyArray())
    }

    fun getRandomVocabulary() = vocaRepository.getRandomVocabulary()

    fun getRandomVocabularies(count: Int, notInclude: RoomVocabulary?): List<RoomVocabulary?> {
        val result = mutableSetOf<RoomVocabulary?>()
        while (result.size < count) {
            val randomVoca = getRandomVocabulary()?.toRoomVocabulary()
            if (randomVoca != notInclude && randomVoca !in result) {
                result.add(randomVoca)
            }
        }
        return result.toList()
    }

    fun isEmpty() = loadVocabulary()?.isEmpty() ?: true

}