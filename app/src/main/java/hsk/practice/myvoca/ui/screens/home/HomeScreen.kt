package hsk.practice.myvoca.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import hsk.practice.myvoca.util.getTimeDiffString
import hsk.practice.myvoca.work.setPeriodicTodayWordWork
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val homeScreenData by viewModel.homeScreenData.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(true) {
        val workManager = WorkManager.getInstance(context)
        setPeriodicTodayWordWork(workManager)
    }

    Loading(
        data = homeScreenData,
        onHelpClose = viewModel::onCloseAlertDialog,
        showTodayWordHelp = viewModel::showTodayWordHelp,
        onRefreshTodayWord = viewModel::onRefreshTodayWord,
        onTodayWordCheckboxChange = viewModel::onTodayWordCheckboxChange
    )
}

@Composable
private fun Loading(
    data: HomeScreenData,
    onHelpClose: () -> Unit,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (data.loading) {
            LoadingIndicator()
        }
        if (data.showTodayWordHelp) {
            TodayWordHelp(onClose = onHelpClose)
        }
        Content(
            data = data,
            showTodayWordHelp = showTodayWordHelp,
            onRefreshTodayWord = onRefreshTodayWord,
            onTodayWordCheckboxChange = onTodayWordCheckboxChange
        )
    }
}

@Composable
private fun Content(
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
        Title(size = data.totalWordCount)
        TodayWords(
            todayWords = data.todayWords,
            lastUpdatedTime = data.todayWordsLastUpdatedTime,
            enableRefresh = data.totalWordCount > 0,
            showTodayWordHelp = showTodayWordHelp,
            onRefreshTodayWord = onRefreshTodayWord,
            onTodayWordCheckboxChange = onTodayWordCheckboxChange
        )
    }
}

@Composable
private fun TodayWords(
    todayWords: ImmutableList<HomeTodayWord>,
    lastUpdatedTime: Long,
    enableRefresh: Boolean,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    TodayWordHeader(
        lastUpdatedTime = lastUpdatedTime,
        showTodayWordHelp = showTodayWordHelp,
        enableRefresh = enableRefresh,
        onRefreshTodayWord = onRefreshTodayWord
    )
    TodayWordItems(
        todayWords = todayWords,
        onTodayWordCheckboxChange = onTodayWordCheckboxChange
    )
}

@Composable
private fun TodayWordItems(
    todayWords: ImmutableList<HomeTodayWord>,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (todayWords.isEmpty()) {
            item {
                TodayWordEmptyIndicator()
            }
        } else {
            items(todayWords) { todayWord ->
                TodayWordContent(
                    todayWord = todayWord,
                    onTodayWordCheckboxChange = onTodayWordCheckboxChange
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TodayWordEmptyIndicator() {
    Box(modifier = Modifier.fillMaxSize()) {
        MyVocaText(
            text = stringResource(R.string.nothing_yet),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun TodayWordHeader(
    lastUpdatedTime: Long,
    showTodayWordHelp: (Boolean) -> Unit,
    enableRefresh: Boolean,
    onRefreshTodayWord: () -> Unit
) {
    val lastUpdatedTimeString = getTimeDiffString(anotherTime = lastUpdatedTime)
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderTitle()
        HelpIcon(showTodayWordHelp = showTodayWordHelp)
        Spacer(modifier = Modifier.weight(1f))
        LastUpdateTimeText(lastUpdatedTimeString = lastUpdatedTimeString)
        RefreshIcon(
            onRefreshTodayWord = onRefreshTodayWord,
            enableRefresh = enableRefresh
        )
    }
}

@Composable
private fun RefreshIcon(onRefreshTodayWord: () -> Unit, enableRefresh: Boolean) {
    IconButton(
        onClick = onRefreshTodayWord,
        enabled = enableRefresh
    ) {
        Icon(
            imageVector = Icons.Outlined.Autorenew,
            contentDescription = stringResource(R.string.refresh_today_word)
        )
    }
}

@Composable
private fun LastUpdateTimeText(lastUpdatedTimeString: String) {
    MyVocaText(
        text = stringResource(R.string.last_updated, lastUpdatedTimeString),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun HelpIcon(showTodayWordHelp: (Boolean) -> Unit) {
    IconButton(
        onClick = { showTodayWordHelp(true) }
    ) {
        Icon(
            imageVector = Icons.Outlined.Help,
            contentDescription = stringResource(R.string.what_is_today_word)
        )
    }
}

@Composable
private fun HeaderTitle() {
    MyVocaText(
        text = stringResource(R.string.today_word),
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun TodayWordContent(
    todayWord: HomeTodayWord,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit
) {
    val alpha by animateFloatAsState(targetValue = if (todayWord.todayWord.checked) 0.6f else 1f)
    val elevation by animateDpAsState(targetValue = if (todayWord.todayWord.checked) 0.dp else 6.dp)
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = Modifier.alpha(alpha)
    ) {
        Box {
            WordContent(
                word = todayWord.vocabulary,
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
private fun Title(size: Int = 0) {
    val titleText = if (size == 0) stringResource(R.string.no_registerd_word) else stringResource(
        R.string.words_are_registed,
        size
    )
    MyVocaText(
        text = titleText,
        maxLines = 2,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TodayWordHelp(
    onClose: () -> Unit
) {
    AlertDialog(
        title = {
            MyVocaText(text = stringResource(id = R.string.what_is_today_word))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MyVocaText(text = stringResource(R.string.memorize_new_words_everyday_words_are_updated_everyday))
                MyVocaText(text = stringResource(R.string.memorized_words_can_be_separated_by_a_check_mark))
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
                    MyVocaText(text = stringResource(R.string.check))
                }
            }
        },
        onDismissRequest = onClose
    )
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() {
    val data = HomeScreenData(
        loading = false,
        totalWordCount = fakeData.size,
        todayWords = fakeData.subList(0, 5).map { voca ->
            HomeTodayWord(TodayWordImpl(wordId = voca.id), voca)
        }.toImmutableList(),
        todayWordsLastUpdatedTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    )
    MyVocaTheme {
        Content(data,
            showTodayWordHelp = {},
            onRefreshTodayWord = {},
            onTodayWordCheckboxChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayWordHelpPreview() {
    MyVocaTheme {
        TodayWordHelp(onClose = {})
    }
}