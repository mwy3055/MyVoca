package hsk.practice.myvoca.ui.state

import org.junit.Assert.assertFalse
import org.junit.Test

class UiStateTest {

    @Test
    fun hasError_True() {
        val state = createState(
            loading = false,
            exception = IllegalStateException()
        )
        assert(state.hasError)
    }

    @Test
    fun hasError_False() {
        val state = createState(
            loading = false,
            exception = null,
            data = 3
        )
        assertFalse(state.hasError)
    }

    @Test
    fun initialLoad_True() {
        val state = createState(
            loading = true,
            exception = null,
            data = null
        )
        assert(state.initialLoading)
    }

    @Test
    fun initialLoad_False1() {
        val state = createState(
            loading = false,
            exception = IndexOutOfBoundsException(),
            data = null
        )
        assertFalse(state.initialLoading)
    }

    @Test
    fun initialLoad_False2() {
        val state = createState(
            loading = true,
            exception = null,
            data = 3
        )
        assertFalse(state.initialLoading)
    }

    @Test
    fun initialLoad_False3() {
        val state = createState(
            loading = false,
            exception = null,
            data = 3
        )
        assertFalse(state.initialLoading)
    }

    private fun createState(
        loading: Boolean,
        exception: Exception? = null,
        data: Int? = null
    ): UiState<Int> = UiState(loading, exception, data)

}