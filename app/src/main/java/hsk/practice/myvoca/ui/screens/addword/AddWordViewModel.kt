package hsk.practice.myvoca.ui.screens.addword

import androidx.lifecycle.ViewModel
import com.hsk.domain.VocaPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.module.LocalVocaPersistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AddWordViewModel @Inject constructor(
    @LocalVocaPersistence private val vocaPersistence: VocaPersistence
) : ViewModel() {

    private val _addWordScreenData = MutableStateFlow(AddWordScreenData())
    val addWordScreenData: StateFlow<AddWordScreenData>
        get() = _addWordScreenData


}

enum class WordExistStatus {
    EXISTS,
    DUPLICATE,
    LOADING,
    WORD_EMPTY
}

data class AddWordScreenData(
    val word: String = "",
    val wordExist: WordExistStatus = WordExistStatus.WORD_EMPTY,
    val meanings: List<MeaningImpl> = emptyList(),
    val memo: String = ""
) {
    fun toVocabularyImpl(): VocabularyImpl {
        val current = System.currentTimeMillis()
        return VocabularyImpl(
            eng = word,
            meaning = meanings,
            addedTime = current,
            lastEditedTime = current,
            memo = memo
        )
    }
}