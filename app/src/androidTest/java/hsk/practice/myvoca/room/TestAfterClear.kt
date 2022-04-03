package hsk.practice.myvoca.room

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
interface TestAfterClear {
    fun clear()

    fun testAfterClear(test: suspend () -> Unit) = runTest {
        clear()
        test()
    }
}