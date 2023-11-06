package hsk.practice.myvoca.ui.screens.allword

import com.hsk.domain.VocaPersistence
import hsk.practice.myvoca.MainCoroutineExtension
import hsk.practice.myvoca.TestSampleData
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.room.persistence.FakeVocaPersistence
import hsk.practice.myvoca.ui.state.UiState
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AllWordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @RegisterExtension
    val coroutineExtension = MainCoroutineExtension(dispatcher)

    private val vocaPersistence: VocaPersistence = FakeVocaPersistence()

    private lateinit var viewModel: AllWordViewModel
    private val uiState: UiState<AllWordData>
        get() = viewModel.allWordUiState.value
    private val uiData: AllWordData
        get() = uiState.data!!

    @BeforeEach
    fun initTest() = runTest {
        vocaPersistence.clearVocabulary()
        vocaPersistence.insertVocabulary(TestSampleData.getSampleVocabularies())
        viewModel = AllWordViewModel(
            vocaPersistence,
            computingDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
    }

    @Test
    fun onQueryTextChanged_EmptyToNotEmpty() {
        val notEmpty = "not empty"
        viewModel.onQueryTextChanged("")
        viewModel.onQueryTextChanged(notEmpty)

        assertThat(uiState.data).isNotNull
        assertThat(uiData.queryState).isNotNull
        assertThat(uiData.queryState.word).isEqualTo(notEmpty)
    }

    @Test
    fun onQueryWordClassToggled_TurnOn() {
        val nounName = WordClassImpl.NOUN.korean
        val nounClass = WordClassImpl.NOUN.toWordClass()
        viewModel.onQueryWordClassToggled(nounName)

        assertThat(uiData.queryState.wordClass).containsOnly(nounClass)
    }

    @Test
    fun onQueryWordClassToggled_OnAndOff() {
        val nounName = WordClassImpl.NOUN.korean
        val nounClass = WordClassImpl.NOUN.toWordClass()
        viewModel.onQueryWordClassToggled(nounName)
        viewModel.onQueryWordClassToggled(nounName)

        assertThat(uiData.queryState.wordClass).doesNotContain(nounClass)
            .isEmpty()
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
            assertThat(uiData.queryState.wordClass).contains(classObject)
        }
    }

    @Test
    fun onSortStateClicked_Alphabet() {
        val sortAlphabet = SortState.Alphabet
        viewModel.onSortStateClicked(sortAlphabet)
        assertThat(uiData.sortState).isEqualTo(sortAlphabet)

        val sortedWords = uiData.currentWordState.sortedBy { it.eng }
        assertThat(uiData.currentWordState).isEqualTo(sortedWords)
    }

    @Test
    fun onSortStateClicked_Random() {
        val sortRandom = SortState.Random
        viewModel.onSortStateClicked(sortRandom)

        assertThat(uiData.sortState).isEqualTo(sortRandom)
    }

    @Test
    fun onClearOption_IsCleared() {
        viewModel.onSortStateClicked(SortState.Latest)
        viewModel.onQueryTextChanged("some query")
        viewModel.onQueryWordClassToggled(WordClassImpl.NOUN.korean)
        viewModel.onClearOption()

        assertThat(uiData.sortState).isEqualTo(SortState.defaultValue)
        assertThat(uiData.queryState.word).isEmpty()
        assertThat(uiData.queryState.wordClass).isEmpty()
    }

    @Test
    fun onWordDelete() = runTest {
        val sample = TestSampleData.getSampleVocaImpl()
        viewModel.onWordDelete(sample).join()

        assertThat(uiData.currentWordState).doesNotContain(sample)
    }
}