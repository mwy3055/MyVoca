package hsk.practice.myvoca.ui.screens.addword

import com.hsk.data.Vocabulary
import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import com.hsk.ktx.zipForEach
import hsk.practice.myvoca.MainCoroutineExtension
import hsk.practice.myvoca.TestSampleData
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toMeaning
import hsk.practice.myvoca.room.persistence.FakeVocaPersistence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AddWordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @RegisterExtension
    val coroutineExtension = MainCoroutineExtension(dispatcher)

    private val vocaPersistence: VocaPersistence = FakeVocaPersistence()

    private lateinit var viewModel: AddWordViewModel
    private val uiState: AddWordScreenData
        get() = viewModel.uiStateFlow.value

    private val uiMeanings: List<MeaningImpl>
        get() = uiState.meanings

    @BeforeEach
    fun initTest() = runBlocking {
        vocaPersistence.clearVocabulary()
        viewModel = AddWordViewModel(vocaPersistence)
    }

    @Test
    fun injectUpdateWord_InjectExistWord(): Unit = runBlocking {
        val sample = vocaPersistence.insertTestData()[0]

        viewModel.injectUpdateTarget(sample.id)
        assertDoesNotThrow {
            viewModel.uiStateFlow.first { it.word == sample.eng }
        }
    }

    @Test
    fun injectUpdateWord_InjectNotExistWord(): Unit = runTest {
        val sample = vocaPersistence.insertTestData()[0]
        viewModel.injectUpdateTarget(sample.id)
        assertDoesNotThrow {
            viewModel.uiStateFlow.first { it.word.isNotEmpty() }
        }
    }

    private suspend fun delayAfter(block: suspend () -> Unit) {
        block()
        delay(50L)
    }

    @Test
    fun onWordUpdate_SingleUpdate() {
        val sample = TestSampleData.getSampleVoca()

        viewModel.onWordUpdate(sample.eng)
        assertThat(uiState.word).isEqualTo(sample.eng)
    }

    @Test
    fun onWordUpdate_ManyUpdates() {
        val sampleEng = TestSampleData.getSampleVoca().eng

        sampleEng.indices.forEach {
            viewModel.onWordUpdate(sampleEng.take(it + 1))
        }
        assertThat(uiState.word).isEqualTo(sampleEng)
    }

    @Test
    fun loadStatus_WordEmpty(): Unit = runBlocking {
        viewModel.loadStatus("")
        assertThat(uiState.wordExistStatus).isEqualTo(WordExistStatus.WORD_EMPTY)
    }

    @Test
    fun loadStatus_NotExists(): Unit = runBlocking {
        viewModel.loadStatus("NotExists")
        assertThat(uiState.wordExistStatus).isEqualTo(WordExistStatus.NOT_EXISTS)
    }

    @Test
    fun loadStatus_Exists(): Unit = runBlocking {
        val sample = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sample))

        viewModel.loadStatus(sample.eng)
        assertThat(uiState.wordExistStatus).isEqualTo(WordExistStatus.DUPLICATE)
    }

    @Test
    fun loadStatus_NewWordIsSame(): Unit = runBlocking {
        val sample = vocaPersistence.insertTestData()[0]
        viewModel.injectUpdateTarget(sample.id)
        viewModel.loadStatus(sample.eng)

        assertDoesNotThrow {
            viewModel.uiStateFlow.first { it.wordExistStatus == WordExistStatus.DUPLICATE }
        }
    }

    @Test
    fun loadStatus_newWordIsDifferent(): Unit = runBlocking {
        val sample = vocaPersistence.insertTestData()[0]
        viewModel.injectUpdateTarget(sample.id)
        viewModel.loadStatus(sample.eng.dropLast(1))

        assertDoesNotThrow {
            viewModel.uiStateFlow.first { it.wordExistStatus == WordExistStatus.NOT_EXISTS }
        }
    }

    @Test
    fun onMeaningAdd_AddOneType() {
        val type = WordClassImpl.NOUN
        viewModel.onMeaningAdd(type)

        assertThat(uiMeanings.size).isEqualTo(1)
        SoftAssertions().apply {
            assertThat(uiMeanings[0].type).isEqualTo(type)
            assertThat(uiMeanings[0].content).isEmpty()
        }.assertAll()
    }

    @Test
    fun onMeaningAdd_AddManyTypes() {
        val types = WordClassImpl.actualValues().take(5).onEach { type ->
            viewModel.onMeaningAdd(type)
        }

        assertThat(uiMeanings.size).isEqualTo(types.size)
        types.zipForEach(uiMeanings) { expectedType, actualMeaning ->
            assertThat(actualMeaning.type).isEqualTo(expectedType)
            assertThat(actualMeaning.content).isEmpty()
        }
    }

    @Test
    fun onMeaningAdd_AddSameTypes() {
        val type = WordClassImpl.ADVERB
        val addNumber = 9
        repeat(addNumber) {
            viewModel.onMeaningAdd(type)
        }

        assertThat(uiMeanings.size).isEqualTo(addNumber)
        uiMeanings.forEach { uiMeaning ->
            assertThat(uiMeaning.type).isEqualTo(type)
            assertThat(uiMeaning.content).isEmpty()
        }
    }

    @Test
    fun onMeaningUpdate_UpdateOneType() {
        val verbType = WordClassImpl.VERB
        viewModel.onMeaningAdd(verbType)

        val meaning = MeaningImpl(verbType, "Something")
        viewModel.onMeaningUpdate(0, meaning)

        assertThat(uiMeanings[0]).isEqualTo(meaning)
    }

    @Test
    fun onMeaningUpdate_UpdateManyTypes() {
        val typeSize = 5
        val types = WordClassImpl.actualValues().takeLast(typeSize)
        types.forEach { viewModel.onMeaningAdd(it) }

        val sampleContent = "some meaning"
        types.forEachIndexed { index, wordClassImpl ->
            viewModel.onMeaningUpdate(index, MeaningImpl(wordClassImpl, sampleContent))
        }

        types.zipForEach(uiMeanings) { type, uiMeaning ->
            assertThat(uiMeaning.type).isEqualTo(type)
            assertThat(uiMeaning.content).isEqualTo(sampleContent)
        }
    }

    @Test
    fun onMeaningUpdate_IndexOutOfBoundsException() {
        assertThrows<IndexOutOfBoundsException> {
            viewModel.onMeaningUpdate(0, MeaningImpl())
        }
    }

    @Test
    fun onMeaningDelete_DeleteOneMeaning() {
        val nounType = WordClassImpl.NOUN
        viewModel.onMeaningAdd(nounType)
        viewModel.onMeaningDelete(0)

        assertThat(uiMeanings).isEmpty()
    }

    @Test
    fun onMeaningDelete_DeleteMeanings() {
        val size = 10
        repeat(size) {
            viewModel.onMeaningAdd(if (it % 2 == 0) WordClassImpl.NOUN else WordClassImpl.VERB)
        }

        (0 until size step 2).reversed().forEach {
            viewModel.onMeaningDelete(it)
        }

        assertThat(uiMeanings).allMatch { it.type == WordClassImpl.VERB }
    }

    @Test
    fun onMeaning_AddUpdateDelete() {
        val meanings = addMeaningsForTest()

        assertThat(uiMeanings.size).isEqualTo(meanings.size)
        meanings.zipForEach(uiMeanings) { meaning, uiMeaning ->
            assertThat(uiMeaning).isEqualTo(meaning)
        }
    }

    private fun addMeaningsForTest(): List<MeaningImpl> {
        val meanings = mutableListOf<MeaningImpl>()

        val addList = listOf(
            WordClassImpl.NOUN,
            WordClassImpl.NOUN,
            WordClassImpl.VERB,
            WordClassImpl.INTERJECTION
        )
        addList.forEach { type ->
            meanings.add(MeaningImpl(type, ""))
            viewModel.onMeaningAdd(type)
        }

        val someMeaning = "some meaning"
        meanings[2] = meanings[2].copy(content = someMeaning)
        viewModel.onMeaningUpdate(2, meanings[2])

        meanings.removeAt(3)
        viewModel.onMeaningDelete(3)

        return meanings
    }

    @Test
    fun onMemoUpdate() {
        val newMemo = "some memo"
        viewModel.onMemoUpdate(newMemo)
        assertThat(uiState.memo).isEqualTo(newMemo)
    }

    @Test
    fun onAddWord_Update(): Unit = runBlocking {
        val sample = vocaPersistence.insertTestData()[0]
        viewModel.injectUpdateTarget(sample.id)
        viewModel.uiStateFlow.first { it.word == sample.eng }

        val newEng = "some new word"
        viewModel.onWordUpdate(newEng)

        viewModel.onAddWord()
        vocaPersistence.waitNewVocabularies()

        val queryResult = vocaPersistence.getVocabulary(VocabularyQuery(word = newEng))
        assertThat(queryResult).isNotEmpty

        val wordInPersistence = queryResult[0]
        assertThat(wordInPersistence.eng).isEqualTo(newEng)
    }

    @Test
    fun onAddWord_Insert(): Unit = runBlocking {
        val eng = "some word"
        val meanings = addMeaningsForTest().map { it.toMeaning() }
        val memo = "some memo"

        viewModel.onWordUpdate(newWord = eng)
        viewModel.onMemoUpdate(memo = memo)

        viewModel.onAddWord()
        vocaPersistence.waitNewVocabularies()

        val queryResult = vocaPersistence.getVocabulary(VocabularyQuery(word = eng))
        assertThat(queryResult).isNotEmpty

        queryResult[0].apply {
            assertThat(this.eng).isEqualTo(eng)
            assertThat(this.meaning).isEqualTo(meanings)
            assertThat(this.memo).isEqualTo(memo)
        }
    }

    private suspend fun VocaPersistence.waitNewVocabularies() {
        // Do not remove println: it passes all the test
        println("wait: ${getAllVocabulary().first()}")
    }

    private suspend fun VocaPersistence.insertTestData(): List<Vocabulary> {
        val sampleData = TestSampleData.getSampleVocabularies()
        insertVocabulary(sampleData)
        return sampleData
    }

}