package database.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import database.Vocabulary
import database.source.local.VocaDao
import database.source.local.VocaDatabase
import hsk.practice.myvoca.AppHelper
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/**
 * VocaRepository mediates between VocaViewModel and VocaRepository.
 * Executes operations at the separate thread.
 *
 * Implemented as Singleton: To unify the management process
 */
class VocaRepository {
    private var vocaDao: VocaDao? = null
    private val executor = Executors.newCachedThreadPool()
    private var database: VocaDatabase? = null
    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null

    private fun loadVocabulary() {
        try {
            executor.execute(LoadTask)
            allVocabulary = LoadTask?.get(10, TimeUnit.SECONDS)
            allVocabulary?.observeForever { vocabularies -> Log.d("HSK APP", "load complete, " + vocabularies?.size) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVocabulary(query: String?): LiveData<MutableList<Vocabulary?>?>? {
        return if (AppHelper.isStringOnlyAlphabet(query)) {
            Log.d("HSK APP", "search by eng: $query")
            vocaDao?.loadVocabularyByEng(query)
        } else {
            Log.d("HSK APP", "search by kor: $query")
            vocaDao?.loadVocabularyByKor(query)
        }
    }

    fun getAllVocabulary(): LiveData<MutableList<Vocabulary?>?>? {
        allVocabulary.takeIf { it == null }?.let { loadVocabulary() }
//        if (allVocabulary == null || allVocabulary.getValue() == null) {
//            loadVocabulary()
//        }
        return allVocabulary
    }

    fun getRandomVocabulary(): LiveData<Vocabulary?> {
        val result = MutableLiveData<Vocabulary?>()
        val random = Random()
        allVocabulary.let {
            if (it == null || it.value == null) {
                loadVocabulary()
                it?.observeForever(object : Observer<MutableList<Vocabulary?>?> {
                    override fun onChanged(vocabularies: MutableList<Vocabulary?>?) {
                        val index = random.nextInt(vocabularies!!.size)
                        result.setValue(vocabularies[index])
                        it.removeObserver(this)
                    }
                })
            } else {
                val index = random.nextInt(allVocabulary!!.value!!.size)
                result.setValue(allVocabulary!!.value?.get(index))
            }
        }
//        if (allVocabulary == null || allVocabulary.getValue() == null) {
//            loadVocabulary()
//            allVocabulary.observeForever(object : Observer<MutableList<Vocabulary?>?> {
//                override fun onChanged(vocabularies: MutableList<Vocabulary?>?) {
//                    val index = random.nextInt(vocabularies.size)
//                    result.setValue(vocabularies.get(index))
//                    allVocabulary.removeObserver(this)
//                }
//            })
//        } else {
//            val index = random.nextInt(allVocabulary.getValue().size)
//            result.setValue(allVocabulary.getValue().get(index))
//        }
        return result
    }

    fun insertVocabulary(vararg vocabularies: Vocabulary?) {
        executor.execute { vocaDao?.insertVocabulary(*vocabularies) }
    }

    fun editVocabulary(vararg vocabularies: Vocabulary?) {
        executor.execute { vocaDao?.updateVocabulary(*vocabularies) }
    }

    fun deleteVocabularies(vararg vocabularies: Vocabulary?) {
        executor.execute { vocaDao?.deleteVocabulary(*vocabularies) }
    }

    private val LoadTask: FutureTask<LiveData<MutableList<Vocabulary?>?>?> = FutureTask { vocaDao?.loadAllVocabulary() }

    companion object {
        private var instance: VocaRepository? = null
        fun getInstance(): VocaRepository? {
            if (instance == null) {
                loadInstance()
            }
            return instance
        }

        fun loadInstance() {
            synchronized(VocaRepository::class.java) {
                if (instance == null) {
                    instance = VocaRepository()
                    instance!!.database = VocaDatabase.Companion.getInstance()
                    instance!!.vocaDao = VocaDatabase.Companion.getInstance()?.vocaDao()
                }
            }
        }
    }
}