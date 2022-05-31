package hsk.practice.myvoca.ui.state

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UiStateTest {

    @Test
    fun hasError_True() {
        val state = createState(
            loading = false,
            exception = IllegalStateException()
        )
        assertThat(state.hasError).isTrue
    }

    @Test
    fun hasError_False() {
        val state = createState(
            loading = false,
            exception = null,
            data = 3
        )
        assertThat(state.hasError).isFalse
    }

    @Test
    fun initialLoad_True() {
        val state = createState(
            loading = true,
            exception = null,
            data = null
        )
        assertThat(state.initialLoading).isTrue
    }

    @Test
    fun initialLoad_False1() {
        val state = createState(
            loading = false,
            exception = IndexOutOfBoundsException(),
            data = null
        )
        assertThat(state.initialLoading).isFalse
    }

    @Test
    fun initialLoad_False2() {
        val state = createState(
            loading = true,
            exception = null,
            data = 3
        )
        assertThat(state.initialLoading).isFalse
    }

    @Test
    fun initialLoad_False3() {
        val state = createState(
            loading = false,
            exception = null,
            data = 3
        )
        assertThat(state.initialLoading).isFalse
    }

    private fun createState(
        loading: Boolean,
        exception: Exception? = null,
        data: Int? = null
    ): UiState<Int> = UiState(loading, exception, data)

}