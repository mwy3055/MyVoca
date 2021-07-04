package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import hsk.practice.myvoca.ui.state.UiState
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AllWordScreen(
    allWordViewModel: AllWordViewModel = viewModel()
) {
    val allWordUiState by allWordViewModel.allWordUiState.collectAsState()

    AllWordLoading(allWordUiState)
}

@Composable
fun AllWordLoading(
    uiState: UiState<AllWordData>
) {
    if (uiState.initialLoad) {
        /* TODO: add full-screen loading composable.
            If it is not initial load, just show the on-screen loading composable. */
        Text(text = "Loading!")
    } else {
        AllWordContent(uiState.data!!)
    }
}

@Composable
fun AllWordContent(
    data: AllWordData
) {
    Text(
        text = "${data.currentWordState.size} vocabularies loaded."
    )
}

@Preview
@Composable
fun AllWordPreview() {
    MyVocaTheme {
        AllWordScreen()
    }
}