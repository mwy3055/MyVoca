package database.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import database.Vocabulary
import database.source.local.VocaDao
import database.source.local.VocaDatabase
import database.source.local.VocaPersistence
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
object VocaRepository {
    private var vocaPersistence: VocaPersistence? = null

    private lateinit var vocaDao: VocaDao
    private lateinit var database: VocaDatabase
    private val executor = Executors.newCachedThreadPool()
    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null

    fun loadInstance(vocaPersistence: VocaPersistence? = null) {
        synchronized(this::class.java) {
            this.vocaPersistence = vocaPersistence
            this.database = VocaDatabase.getInstance()!!
            this.vocaDao = this.database.vocaDao()!!
        }
    }

    private fun loadVocabulary() {
        try {
            executor.execute(loadTask)
            allVocabulary = loadTask.get(10, TimeUnit.SECONDS)
            allVocabulary?.observeForever { vocabularies -> Log.d("HSK APP", "load complete, " + vocabularies?.size) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVocabulary(query: String?): LiveData<MutableList<Vocabulary?>?>? {
        return if (AppHelper.isStringOnlyAlphabet(query)) {
            Log.d("HSK APP", "search by eng: $query")
            vocaDao.loadVocabularyByEng(query)
        } else {
            Log.d("HSK APP", "search by kor: $query")
            vocaDao.loadVocabularyByKor(query)
        }
    }

    fun getAllVocabulary(): LiveData<MutableList<Vocabulary?>?>? {
        if (allVocabulary == null) {
            loadVocabulary()
        }
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
                it?.observeForever(object : Observer<List<Vocabulary?>?> {
                    override fun onChanged(vocabularies: List<Vocabulary?>?) {
                        val index = random.nextInt(vocabularies!!.size)
                        result.value = vocabularies[index]
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
        executor.execute { vocaDao.insertVocabulary(*vocabularies) }
    }

    fun editVocabulary(vararg vocabularies: Vocabulary?) {
        executor.execute { vocaDao.updateVocabulary(*vocabularies) }
    }

    fun deleteVocabularies(vararg vocabularies: Vocabulary?) {
        executor.execute { vocaDao.deleteVocabulary(*vocabularies) }
    }

    private val loadTask = FutureTask { vocaDao.loadAllVocabulary() }
}