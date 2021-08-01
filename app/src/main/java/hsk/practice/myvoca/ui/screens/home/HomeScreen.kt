package hsk.practice.myvoca.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Help
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val homeScreenData by viewModel.homeScreenData.collectAsState()

    HomeLoading(
        data = homeScreenData,
        showTodayWordHelp = viewModel::showTodayWordHelp,
        onRefreshTodayWord = viewModel::onRefreshTodayWord,
        onTodayWordCheckboxChange = viewModel::onTodayWordCheckboxChange
    )
}

@Composable
fun HomeLoading(
    data: HomeScreenData,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (data.loading) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
            )
        }
        if (data.showTodayWordHelp) {
            HomeTodayWordHelp()
        }
        HomeContent(
            data = data,
            showTodayWordHelp = showTodayWordHelp,
            onRefreshTodayWord = onRefreshTodayWord,
            onTodayWordCheckboxChange = onTodayWordCheckboxChange
        )
    }
}

@Composable
fun HomeContent(
    data: HomeScreenData,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeTitle(size = data.totalWordCount)
        HomeTodayWords(
            data.todayWords,
            showTodayWordHelp = showTodayWordHelp,
            onRefreshTodayWord = onRefreshTodayWord,
            onTodayWordCheckboxChange = onTodayWordCheckboxChange
        )
    }
}

@Composable
fun HomeTodayWords(
    todayWords: List<HomeTodayWord>,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            HomeTodayWordHeader(
                showTodayWordHelp = showTodayWordHelp,
                onRefreshTodayWord = onRefreshTodayWord
            )
        }
        if (todayWords.isEmpty()) {
            item {
                HomeTodayWordEmpty()
            }
        } else {
            items(todayWords) { todayWord ->
                Card(elevation = 6.dp) {
                    HomeTodayWord(
                        todayWord,
                        onTodayWordCheckboxChange = onTodayWordCheckboxChange
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTodayWordEmpty() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "아직 아무것도 없습니다.",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun HomeTodayWordHeader(
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "오늘의 단어",
            style = MaterialTheme.typography.h6
        )
        IconButton(
            onClick = { showTodayWordHelp(true) }
        ) {
            Icon(
                imageVector = Icons.Outlined.Help,
                contentDescription = "오늘의 단어란?"
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onRefreshTodayWord
        ) {
            Icon(
                imageVector = Icons.Outlined.Autorenew,
                contentDescription = "오늘의 단어 새로고침하기"
            )
        }
    }
}

@Composable
fun HomeTodayWord(
    todayWord: HomeTodayWord,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    Box {
        WordContent(todayWord.vocabulary)
        Checkbox(
            checked = todayWord.todayWord.checked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            onCheckedChange = { onTodayWordCheckboxChange(todayWord) }
        )
    }
}

@Composable
fun HomeTitle(size: Int = 0) {
    val titleText = if (size == 0) "등록된 단어가\n없습니다" else "${size}개의 단어가\n등록되어 있어요"
    Text(
        text = titleText,
        maxLines = 2,
        style = MaterialTheme.typography.h4,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HomeTodayWordHelp() {
    // TODO: AlertDialog
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    val data = HomeScreenData(
        loading = false,
        totalWordCount = fakeData.size,
        todayWords = fakeData.subList(0, 5).map { voca ->
            HomeTodayWord(TodayWordImpl(wordId = voca.id), voca)
        }
    )
    MyVocaTheme {
        HomeContent(data,
            showTodayWordHelp = {},
            onRefreshTodayWord = {},
            onTodayWordCheckboxChange = {}
        )
    }
}