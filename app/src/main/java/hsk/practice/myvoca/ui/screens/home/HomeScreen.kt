package hsk.practice.myvoca.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import hsk.practice.myvoca.util.getTimeDiffString
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val homeScreenData by viewModel.homeScreenData.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeLoading(
            data = homeScreenData,
            onHelpClose = viewModel::onCloseAlertDialog,
            showTodayWordHelp = viewModel::showTodayWordHelp,
            onRefreshTodayWord = viewModel::onRefreshTodayWord,
            onTodayWordCheckboxChange = viewModel::onTodayWordCheckboxChange
        )
    }
}

@Composable
fun HomeLoading(
    data: HomeScreenData,
    onHelpClose: () -> Unit,
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
            HomeTodayWordHelp(onClose = onHelpClose)
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
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeTitle(size = data.totalWordCount)
        HomeTodayWords(
            todayWords = data.todayWords,
            lastUpdatedTime = data.todayWordsLastUpdatedTime,
            showTodayWordHelp = showTodayWordHelp,
            onRefreshTodayWord = onRefreshTodayWord,
            onTodayWordCheckboxChange = onTodayWordCheckboxChange
        )
    }
}

@Composable
fun HomeTodayWords(
    todayWords: List<HomeTodayWord>,
    lastUpdatedTime: Long,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    // TODO: animate content change (by clicking checkbox...)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            HomeTodayWordHeader(
                lastUpdatedTime = lastUpdatedTime,
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
                HomeTodayWord(
                    todayWord,
                    onTodayWordCheckboxChange = onTodayWordCheckboxChange
                )
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
    lastUpdatedTime: Long,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit
) {
    val lastUpdatedTimeString =
        getTimeDiffString(LocalDateTime.ofEpochSecond(lastUpdatedTime, 0, ZoneOffset.UTC))
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
        Text(
            text = "마지막 업데이트: $lastUpdatedTimeString",
            style = MaterialTheme.typography.caption
        )
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
    val alpha by animateFloatAsState(targetValue = if (todayWord.todayWord.checked) 0.6f else 1f)
    val elevation by animateDpAsState(targetValue = if (todayWord.todayWord.checked) 0.dp else 6.dp)
    Card(
        elevation = elevation,
        modifier = Modifier.alpha(alpha)
    ) {
        Box {
            WordContent(
                todayWord.vocabulary,
                showExpandButton = false,
                expanded = true,
                onExpanded = {}
            )
            Checkbox(
                checked = todayWord.todayWord.checked,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                onCheckedChange = { onTodayWordCheckboxChange(todayWord) }
            )
        }
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
fun HomeTodayWordHelp(
    onClose: () -> Unit
) {
    // TODO: AlertDialog
    AlertDialog(
        title = {
            Text(text = "오늘의 단어란?")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "매일매일 새로운 단어를 외워 보세요. 단어는 하루마다 갱신됩니다.")
                Text(text = "외운 단어는 체크 표시로 구분할 수 있습니다.")
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onClose) {
                    Text("확인")
                }
            }
        },
        onDismissRequest = onClose
    )
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    val data = HomeScreenData(
        loading = false,
        totalWordCount = fakeData.size,
        todayWords = fakeData.subList(0, 5).map { voca ->
            HomeTodayWord(TodayWordImpl(wordId = voca.id), voca)
        },
        todayWordsLastUpdatedTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    )
    MyVocaTheme {
        HomeContent(data,
            showTodayWordHelp = {},
            onRefreshTodayWord = {},
            onTodayWordCheckboxChange = {}
        )
    }
}