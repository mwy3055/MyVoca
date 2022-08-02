package hsk.practice.myvoca

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine Rule for JUnit4
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(private val dispatcher: TestDispatcher) : TestWatcher(), CoroutineScope {
    override val coroutineContext: CoroutineContext = dispatcher + Job()

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        dispatcher.cancel()
    }

    override fun apply(base: Statement?, description: Description?) = object : Statement() {
        override fun evaluate() {
            this@MainCoroutineRule.cancel()
        }
    }
}