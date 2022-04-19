package hsk.practice.myvoca.ui.screens.allword

import com.hsk.domain.VocaPersistence
import hsk.practice.myvoca.TestSampleData
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.room.persistence.FakeVocaPersistence
import hsk.practice.myvoca.room.vocabulary.toVocabularyImpl
import hsk.practice.myvoca.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AllWordViewModelTest {

    private val vocaPersistence: VocaPersistence = FakeVocaPersistence()

    private lateinit var viewModel: AllWordViewModel
    private val uiState: UiState<AllWordData>
        get() = viewModel.allWordUiState.value
    private val uiData: AllWordData
        get() = uiState.data!!

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Before
    fun initTest() = runBlocking {
        vocaPersistence.clearVocabulary()
        viewModel = AllWordViewModel(vocaPersistence)
        vocaPersistence.insertVocabulary(TestSampleData.getSampleVocabularies())
        delay(100)
    }


    @Test
    fun onQueryTextChanged_EmptyToNotEmpty() {
        val notEmpty = "not empty"
        viewModel.onQueryTextChanged("")
        viewModel.onQueryTextChanged(notEmpty)

        assertNotNull(uiState.data)

        assertNotNull(uiData.queryState)
        assertEquals(notEmpty, uiData.queryState.word)
    }

    @Test
    fun onQueryWordClassToggled_TurnOn() {
        val nounName = WordClassImpl.NOUN.korean
        val nounClass = WordClassImpl.NOUN.toWordClass()
        viewModel.onQueryWordClassToggled(nounName)

        assert(uiData.queryState.wordClass.contains(nounClass))
    }

    @Test
    fun onQueryWordClassToggled_OnAndOff() {
        val nounName = WordClassImpl.NOUN.korean
        val nounClass = WordClassImpl.NOUN.toWordClass()
        viewModel.onQueryWordClassToggled(nounName)
        viewModel.onQueryWordClassToggled(nounName)

        assertFalse(uiData.queryState.wordClass.contains(nounClass))
    }

    @Test
    fun onQueryWordClassToggled_ToggleMany() {
        val classImpls = WordClassImpl.actualValues().take(5)
        val classNames = classImpls.map { it.korean }
        val classObjects = classImpls.map { it.toWordClass() }
        classNames.forEach { className ->
            viewModel.onQueryWordClassToggled(className)
        }

        classObjects.forEach { classObject ->
            assert(uiData.queryState.wordClass.contains(classObject))
        }
    }

    @Test
    fun onSortStateClicked_Alphabet() {
        val alphabetState = SortState.Alphabet
        viewModel.onSortStateClicked(alphabetState)
        assertEquals(alphabetState, uiData.sortState)

        val sortedList = uiData.currentWordState.sortedBy { it.eng }
        assertEquals(sortedList, uiData.currentWordState)
    }

    @Test
    fun onSortStateClicked_Random() {
        val randomState = SortState.Random
        viewModel.onSortStateClicked(randomState)

        assertEquals(randomState, uiData.sortState)
    }

    @Test
    fun onClearOption_IsCleared() {
        viewModel.onSortStateClicked(SortState.Latest)
        viewModel.onQueryTextChanged("some query")
        viewModel.onQueryWordClassToggled(WordClassImpl.NOUN.korean)

        viewModel.onClearOption()

        assertEquals(SortState.defaultValue, uiData.sortState)
        assert(uiData.queryState.word.isEmpty())
        assert(uiData.queryState.wordClass.isEmpty())
    }

    @Test
    fun onWordDelete() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        viewModel.onWordDelete(sampleWord.toVocabularyImpl())
        delay(200)

        assertNull(vocaPersistence.getVocabularyById(sampleWord.id))
    }


}