package hsk.practice.myvoca.ui.screens.home

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import hsk.practice.myvoca.MainCoroutineRule
import hsk.practice.myvoca.TestSampleData
import hsk.practice.myvoca.app.MyVocaApplication
import hsk.practice.myvoca.data.toTodayWordImpl
import hsk.practice.myvoca.room.persistence.FakeTodayWordPersistence
import hsk.practice.myvoca.room.persistence.FakeVocaPersistence
import hsk.practice.myvoca.util.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val vocaPersistence: VocaPersistence = FakeVocaPersistence()
    private val todayWordPersistence: TodayWordPersistence =
        FakeTodayWordPersistence(vocaPersistence)
    private val workManager: WorkManager
    private val dataStore: PreferencesDataStore

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(testDispatcher)

    private lateinit var viewModel: HomeViewModel
    private val uiData: HomeScreenData
        get() = viewModel.homeScreenData.value

    init {
        val application = ApplicationProvider.getApplicationContext<MyVocaApplication>()
        workManager = WorkManager.getInstance(application)
        dataStore = PreferencesDataStore(application)
    }

    @Before
    fun setUp() = runTest {
        vocaPersistence.clearVocabulary()
        todayWordPersistence.clearTodayWords()
        viewModel = HomeViewModel(
            workManager = workManager,
            vocaPersistence = vocaPersistence,
            todayWordPersistence = todayWordPersistence,
            dataStore = dataStore,
            computingDispatcher = testDispatcher,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun showTodayWordHelp_TurnOn() {
        viewModel.showTodayWordHelp(true)
        assertThat(uiData.showTodayWordHelp).isTrue
    }

    @Test
    fun showTodayWordHelp_Toggle() {
        viewModel.showTodayWordHelp(true)
        viewModel.showTodayWordHelp(false)
        assertThat(uiData.showTodayWordHelp).isFalse
    }

    @Test
    fun onTodayWordCheckboxChange_CheckPersistence() = runTest {
        vocaPersistence.insertVocabulary(TestSampleData.getSampleVocabularies())
        val sample = TestSampleData.getSampleTodayWord()
        todayWordPersistence.insertTodayWord(sample)
        viewModel.onTodayWordCheckboxChange(
            HomeTodayWord(
                sample.toTodayWordImpl(),
                TestSampleData.getSampleVocaImpl()
            )
        )
        assertDoesNotThrow {
            todayWordPersistence.loadTodayWords().first { candidate ->
                candidate.any { it.todayId == sample.todayId && it.checked != sample.checked }
            }
        }
    }

    @Test
    fun onCloseAlertDialog_NormalCase() {
        viewModel.showTodayWordHelp(true)
        viewModel.onCloseAlertDialog()
        assertThat(uiData.showTodayWordHelp).isFalse
    }
}