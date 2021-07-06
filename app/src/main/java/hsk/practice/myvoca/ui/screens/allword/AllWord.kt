package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.state.UiState
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.coroutines.launch

@Composable
fun AllWordScreen(
    allWordViewModel: AllWordViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val allWordUiState by allWordViewModel.allWordUiState.collectAsState()

    AllWordLoading(
        uiState = allWordUiState,
        onRefresh = { coroutineScope.launch { allWordViewModel.refreshChannel.send(Unit) } }
    )
}

@Composable
fun AllWordLoading(
    uiState: UiState<AllWordData>,
    onRefresh: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.data?.let { data ->
            AllWordContent(
                data = data,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
fun AllWordContent(
    data: AllWordData,
    onRefresh: () -> Unit
) {
    Column {
        Text(
            text = "${data.currentWordState.size} vocabularies loaded.",
            modifier = Modifier.clickable(onClick = onRefresh)
        )
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
        AllWordItems(data.currentWordState)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AllWordItems(words: List<VocabularyImpl>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(words) { word ->
            WordContent(word)
        }
    }
}

@Preview
@Composable
fun AllWordItemsPreview() {
    MyVocaTheme {
        AllWordItems(fakeData)
    }
}