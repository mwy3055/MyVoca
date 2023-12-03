package hsk.practice.myvoca.ui.state

data class UiState<T>(
    val loading: Boolean = false,
    val exception: Exception? = null,
    val data: T? = null,
) {
    val hasError: Boolean
        get() = exception != null

    val initialLoading: Boolean
        get() = loading && !hasError && data == null
}