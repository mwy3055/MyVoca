package hsk.practice.myvoca.ui.home

import androidx.lifecycle.*
import com.hsk.data.VocaPersistence
import com.hsk.data.VocaRepository
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.toRoomVocabulary
import kotlinx.coroutines.launch

/**
 * ViewModel provides data used in the fragment.
 * But here, all data is managed by the VocaViewModel.
 * There is no fragment-dependent data in this application, so each ViewModel has nothing to do.
 * Just left for further use.
 */
class HomeViewModel(vocaPersistence: VocaPersistence) : ViewModel() {

    private val vocaRepository: VocaRepository = VocaRepository(vocaPersistence)

    private val _randomVocabulary = MutableLiveData<RoomVocabulary>()
    val randomVocabulary: LiveData<RoomVocabulary>
        get() = _randomVocabulary

    val vocabularyEng: LiveData<String> = Transformations.map(randomVocabulary) { vocabulary ->
        vocabulary.eng
    }

    val vocabularyKor: LiveData<String> = Transformations.map(randomVocabulary) { vocabulary ->
        vocabulary.kor
    }

    val vocabularySize: LiveData<Int> = Transformations.map(vocaRepository.getAllVocabulary().asLiveData(viewModelScope.coroutineContext)) {
        it.size
    }

    fun loadRandomVocabulary() = viewModelScope.launch {
        _randomVocabulary.value = vocaRepository.getRandomVocabulary()?.toRoomVocabulary()
                ?: RoomVocabulary.nullVocabulary
    }

}