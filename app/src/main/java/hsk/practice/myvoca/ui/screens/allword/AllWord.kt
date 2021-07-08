package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageSearch
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

@Composable
fun AllWordScreen(
    allWordViewModel: AllWordViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val allWordUiState by allWordViewModel.allWordUiState.collectAsState()

    AllWordLoading(
        uiState = allWordUiState,
        onSearchOptionClicked = allWordViewModel::onSearchOptionClicked
    )
}

@Composable
fun AllWordLoading(
    uiState: UiState<AllWordData>,
    onSearchOptionClicked: () -> Unit
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
                onSearchOptionClicked = onSearchOptionClicked
            )
        }
    }
}

@Composable
fun AllWordContent(
    data: AllWordData,
    onSearchOptionClicked: () -> Unit
) {
    Column {
        AllWordHeader(
            vocabularySize = data.currentWordState.size,
            onSearchOptionClicked = onSearchOptionClicked
        )
        AllWordItems(data.currentWordState)
    }
}

@Composable
fun AllWordHeader(
    vocabularySize: Int,
    onSearchOptionClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(8.dp)
    ) {
        Text(
            text = "검색 결과: ${vocabularySize}개",
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        TextButton(onClick = onSearchOptionClicked) {
            Icon(
                imageVector = Icons.Filled.ManageSearch,
                contentDescription = null
            )
            Text(text = "검색 옵션")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AllWordItems(words: List<VocabularyImpl>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = words,
            key = { it.id }
        ) { word ->
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

@Preview
@Composable
fun AllWordHeaderPreview() {
    MyVocaTheme {
        AllWordHeader(vocabularySize = 10) {

        }
    }
}