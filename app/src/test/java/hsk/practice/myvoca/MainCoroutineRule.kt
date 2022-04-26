package hsk.practice.myvoca

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(private val dispatcher: TestDispatcher) : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        dispatcher.cancel()
        Dispatchers.resetMain()
    }
}

suspend fun <T> withDelay(
    context: CoroutineContext,
    delayMilli: Long = 50L,
    block: suspend CoroutineScope.() -> T
): T = withContext(context) {
    val result = block()
    delay(delayMilli)
    return@withContext result
}