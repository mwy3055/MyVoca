package hsk.practice.myvoca

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import database.Vocabulary
import database.source.VocaRepository
import java.util.*

/**
 * VocaViewModel is at the top of the database abstraction.
 * ViewModel interacts with the UI classes directly.
 * All database operations must be done through this class.
 *
 * Also, all methods work asynchronously because database access is costly.
 * Methods return the LiveData immediately when the method is called. Actual result will be filled into LiveData later.
 * UI classes should observe the LiveData and define what to do when the operation is actually finished.
 */
class VocaViewModel : ViewModel() {

    private var allVocabularies: LiveData<MutableList<Vocabulary?>?>? = null
    private val vocabulary: Vocabulary? = null
    private val vocaRepo: VocaRepository? = VocaRepository.getInstance()

    fun getAllVocabulary(): LiveData<MutableList<Vocabulary?>?>? {
        if (allVocabularies?.value == null) {
            loadVocabularies()
        }
        return allVocabularies
    }

    fun getVocabularyCount(): LiveData<Int?> {
        val result = MutableLiveData<Int?>()
        if (allVocabularies?.value == null) {
            loadVocabularies()
            allVocabularies?.observeForever(object : Observer<MutableList<Vocabulary?>?> {
                override fun onChanged(vocabularies: MutableList<Vocabulary?>?) {
                    result.setValue(vocabularies?.size)
                    allVocabularies?.removeObserver(this)
                }
            })
        } else {
            result.setValue(allVocabularies!!.value?.size)
        }
        return result
    }

    fun deleteVocabulary(vararg vocabularies: Vocabulary?) {
        vocaRepo?.deleteVocabularies(*vocabularies)
    }

    fun getVocabulary(query: String?): LiveData<MutableList<Vocabulary?>?>? = vocaRepo?.getVocabulary(query)

    fun insertVocabulary(vararg vocabularies: Vocabulary?) = vocaRepo?.insertVocabulary(*vocabularies)

    @Synchronized
    private fun loadVocabularies() {
        // do what?
        allVocabularies = vocaRepo?.getAllVocabulary()
    }

    fun editVocabulary(vocabulary: Vocabulary?) {
        vocaRepo?.editVocabulary(vocabulary)
    }

    fun getRandomVocabulary() = vocaRepo?.getRandomVocabulary()

    fun getRandomVocabularies(count: Int, notInclude: Vocabulary?): MutableList<Vocabulary?>? {
        val result = ArrayList<Vocabulary?>()
        while (result.size < count) {
            val voca = vocaRepo?.getRandomVocabulary()?.value
            if (voca != notInclude) {
                result.add(voca)
            }
        }
        return result
    }

    fun isEmpty(): LiveData<Boolean?> {
        val result = MutableLiveData(true)
        if (allVocabularies?.value == null) {
            loadVocabularies()
            allVocabularies?.observeForever(object : Observer<MutableList<Vocabulary?>?> {
                override fun onChanged(vocabularies: MutableList<Vocabulary?>?) {
                    result.value = (vocabularies?.size == 0)
                    allVocabularies!!.removeObserver(this)
                }
            })
        } else {
            result.setValue(allVocabularies!!.value?.size == 0)
        }
        return result
    }

    init {
        loadVocabularies()
    }
}