package hsk.practice.myvoca.ui.screens.addword

import com.hsk.data.VocabularyQuery
import com.hsk.domain.VocaPersistence
import hsk.practice.myvoca.MainCoroutineExtension
import hsk.practice.myvoca.TestSampleData
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.toMeaning
import hsk.practice.myvoca.room.persistence.FakeVocaPersistence
import hsk.practice.myvoca.util.zipForEach
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        delayAfter {
            vocaPersistence.clearVocabulary()
            viewModel = AddWordViewModel(vocaPersistence)
        }
    }

    @Test
    fun injectUpdateWord_InjectExistWord() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sampleWord))

        injectUpdateWordThenDelay(sampleWord.id)
        assertEquals(sampleWord.eng, uiState.word)
    }

    @Test
    fun injectUpdateWord_InjectNotExistWord() = runBlocking {
        injectUpdateWordThenDelay(1)

        assert(uiState.word.isEmpty())
    }

    private suspend fun injectUpdateWordThenDelay(wordId: Int) = delayAfter {
        viewModel.injectUpdateTarget(wordId)
    }


    private suspend fun delayAfter(block: suspend () -> Unit) {
        block()
        delay(50L)
    }

    // onWordUpdate
    @Test
    fun onWordUpdate_SingleUpdate() {
        val sampleWord = TestSampleData.getSampleVoca()

        viewModel.onWordUpdate(sampleWord.eng)
        assertEquals(sampleWord.eng, uiState.word)
    }

    @Test
    fun onWordUpdate_ManyUpdates() {
        val sampleWordEng = TestSampleData.getSampleVoca().eng

        sampleWordEng.indices.forEach {
            viewModel.onWordUpdate(sampleWordEng.take(it + 1))
        }
        assertEquals(sampleWordEng, uiState.word)
    }

    @Test
    fun loadStatus_WordEmpty() = runBlocking {
        viewModel.loadStatus("")
        assertEquals(WordExistStatus.WORD_EMPTY, uiState.wordExistStatus)
    }

    @Test
    fun loadStatus_NotExists() = runBlocking {
        viewModel.loadStatus("NotExists")
        assertEquals(WordExistStatus.NOT_EXISTS, uiState.wordExistStatus)
    }

    @Test
    fun loadStatus_Exists() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sampleWord))

        viewModel.loadStatus(sampleWord.eng)
        assertEquals(WordExistStatus.DUPLICATE, uiState.wordExistStatus)
    }

    @Test
    fun loadStatus_NewWordIsSame() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sampleWord))

        injectUpdateWordThenDelay(sampleWord.id)

        viewModel.loadStatus(sampleWord.eng)
        assertEquals(WordExistStatus.NOT_EXISTS, uiState.wordExistStatus)
    }

    @Test
    fun loadStatus_newWordIsDifferent() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sampleWord))

        injectUpdateWordThenDelay(sampleWord.id)

        viewModel.loadStatus(sampleWord.eng.dropLast(1))
        assertEquals(WordExistStatus.NOT_EXISTS, uiState.wordExistStatus)
    }


    //    - onMeaningAdd

    @Test
    fun onMeaningAdd_AddOneType() {
        val type = WordClassImpl.NOUN
        viewModel.onMeaningAdd(type)

        assertEquals(1, uiMeanings.size)
        assertEquals(type, uiMeanings[0].type)
        assert(uiMeanings[0].content.isEmpty())
    }

    @Test
    fun onMeaningAdd_AddManyTypes() {
        val types = WordClassImpl.actualValues().take(5)
        types.forEach { type ->
            viewModel.onMeaningAdd(type)
        }

        assertEquals(types.size, uiMeanings.size)
        types.zipForEach(uiMeanings) { expectedType, actualMeaning ->
            assertEquals(expectedType, actualMeaning.type)
            assert(actualMeaning.content.isEmpty())
        }
    }

    @Test
    fun onMeaningAdd_AddSameTypes() {
        val type = WordClassImpl.ADVERB
        val addNumber = 9
        repeat(addNumber) {
            viewModel.onMeaningAdd(type)
        }

        assertEquals(addNumber, uiMeanings.size)
        uiMeanings.forEach { uiMeaning ->
            assertEquals(type, uiMeaning.type)
            assert(uiMeaning.content.isEmpty())
        }
    }

    //    - onMeaningUpdate
    @Test
    fun onMeaningUpdate_UpdateOneType() {
        val verbType = WordClassImpl.VERB
        viewModel.onMeaningAdd(verbType)

        val meaning = MeaningImpl(verbType, "Something")
        viewModel.onMeaningUpdate(0, meaning)

        assertEquals(meaning, uiMeanings[0])
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
            assertEquals(type, uiMeaning.type)
            assertEquals(sampleContent, uiMeaning.content)
        }
    }

    @Test
    fun onMeaningUpdate_IndexOutOfBoundsException() {
        var exceptionOccur = false
        try {
            viewModel.onMeaningUpdate(0, MeaningImpl())
        } catch (e: IndexOutOfBoundsException) {
            exceptionOccur = true
        }
        assert(exceptionOccur)
    }

    //    - onMeaningDelete
    @Test
    fun onMeaningDelete_DeleteOneMeaning() {
        val nounType = WordClassImpl.NOUN
        viewModel.onMeaningAdd(nounType)

        viewModel.onMeaningDelete(0)
        assert(uiMeanings.isEmpty())
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

        uiMeanings.forEach { uiMeaning ->
            assertEquals(WordClassImpl.VERB, uiMeaning.type)
        }
    }

    // Meaning 사용 복합
    @Test
    fun onMeaning_AddUpdateDelete() {
        val meanings = addMeaningsForTest()

        assertEquals(meanings.size, uiMeanings.size)
        meanings.zipForEach(uiMeanings) { meaning, uiMeaning ->
            assertEquals(meaning, uiMeaning)
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

        assertEquals(newMemo, uiState.memo)
    }

    //    - onAddWord

    @Test
    fun onAddWord_Update() = runBlocking {
        val sampleWord = TestSampleData.getSampleVoca()
        vocaPersistence.insertVocabulary(listOf(sampleWord))

        injectUpdateWordThenDelay(sampleWord.id)

        val newEng = "some new word"
        viewModel.onWordUpdate(newEng)

        onAddWordThenDelay()

        val queryResult = vocaPersistence.getVocabulary(VocabularyQuery(word = newEng))
        assert(queryResult.isNotEmpty())

        val wordInPersistence = queryResult[0]
        assertEquals(newEng, wordInPersistence.eng)
    }

    @Test
    fun onAddWord_Insert() = runBlocking {
        val eng = "some word"
        val meanings = addMeaningsForTest().map { it.toMeaning() }
        val memo = "some memo"

        viewModel.onWordUpdate(newWord = eng)
        viewModel.onMemoUpdate(memo = memo)

        onAddWordThenDelay()

        val queryResult = vocaPersistence.getVocabulary(VocabularyQuery(word = eng))
        assert(queryResult.isNotEmpty())

        val wordInPersistence = queryResult[0]
        assertEquals(eng, wordInPersistence.eng)
        assertEquals(meanings, wordInPersistence.meaning)
        assertEquals(memo, wordInPersistence.memo)
    }

    private suspend fun onAddWordThenDelay() = delayAfter {
        viewModel.onAddWord()
    }


}