package hsk.practice.myvoca.ui.home

import androidx.lifecycle.*
import com.hsk.data.VocaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.VocabularyImpl
import hsk.practice.myvoca.framework.toVocabularyImpl
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel provides data used in the fragment.
 * But here, all data is managed by the VocaViewModel.
 * There is no fragment-dependent data in this application, so each ViewModel has nothing to do.
 * Just left for further use.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(@RoomVocaRepository val vocaRepository: VocaRepository) :
    ViewModel() {

    private val _randomVocabulary = MutableLiveData<VocabularyImpl>()
    val randomVocabulary: LiveData<VocabularyImpl>
        get() = _randomVocabulary

    val vocabularyEng: LiveData<String> = Transformations.map(randomVocabulary) { vocabulary ->
        vocabulary.eng
    }

    val vocabularyKor: LiveData<String> = Transformations.map(randomVocabulary) { vocabulary ->
        vocabulary.kor
    }

    val vocabularySize: LiveData<Int> = Transformations.map(
        vocaRepository.getAllVocabulary().asLiveData(viewModelScope.coroutineContext)
    ) {
        it.size
    }

    val vocabularyNotEmpty: LiveData<Boolean> = Transformations.map(vocabularySize) {
        it > 0
    }

    fun loadRandomVocabulary() = viewModelScope.launch {
        _randomVocabulary.value = vocaRepository.getRandomVocabulary()?.toVocabularyImpl()
            ?: VocabularyImpl.nullVocabulary
    }

}