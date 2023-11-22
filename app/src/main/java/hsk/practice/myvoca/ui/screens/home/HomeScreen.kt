package hsk.practice.myvoca.ui.screens.home

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.TodayWordImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.AddWordButton
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import hsk.practice.myvoca.util.getTimeDiffString
import hsk.practice.myvoca.work.setPeriodicTodayWordWork
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime
import java.time.ZoneOffset

// TODO 단어 추가 스낵바?
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
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
        onTodayWordCheckboxChange = viewModel::onTodayWordCheckboxChange,
        modifier = modifier
    )
}

@Composable
private fun Loading(
    data: HomeScreenData,
    onHelpClose: () -> Unit,
    showTodayWordHelp: (Boolean) -> Unit,
    onRefreshTodayWord: () -> Unit,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
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
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        if (data.todayWords.isEmpty()) {
            TodayWordTitle(
                modifier = modifier.padding(top = 16.dp)
            )
        }
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
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit,
    modifier: Modifier = Modifier,
) {
    TodayWordHeader(
        titleTextStyle = if (todayWords.isEmpty()) MaterialTheme.typography.titleLarge
        else MaterialTheme.typography.headlineLarge,
        showUpdateInfo = !todayWords.isEmpty(),
        lastUpdatedTime = lastUpdatedTime,
        showTodayWordHelp = showTodayWordHelp,
        enableRefresh = enableRefresh,
        onRefreshTodayWord = onRefreshTodayWord,
        modifier = if (todayWords.isEmpty()) modifier.padding(top = 21.dp)
        else modifier.padding(top = 31.dp)
    )
    Spacer(modifier.height(15.dp))
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (todayWords.isEmpty()) {
            TodayWordEmptyIndicator(
                modifier = modifier.align(Alignment.Center)
            )
        } else {
            TodayWordItems(
                todayWords = todayWords,
                onTodayWordCheckboxChange = onTodayWordCheckboxChange
            )
        }
    }
}

@Composable
private fun TodayWordItems(
    todayWords: ImmutableList<HomeTodayWord>,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = todayWords,
            key = { it.hashCode() }
        ) { todayWord ->
            TodayWordContent(
                todayWord = todayWord,
                onTodayWordCheckboxChange = onTodayWordCheckboxChange
            )
        }
    }
}

@Composable
private fun TodayWordEmptyIndicator(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyVocaText(
                text = stringResource(R.string.please_register_the_word_first),
                style = MaterialTheme.typography.headlineMedium
            )
            AddWordButton(
                onClick = {
                    context.startActivity(
                        Intent(context, AddWordActivity::class.java)
                    )
                }
            )
        }
    }
}

@Composable
private fun TodayWordHeader(
    titleTextStyle: TextStyle,
    showUpdateInfo: Boolean,
    lastUpdatedTime: Long,
    showTodayWordHelp: (Boolean) -> Unit,
    enableRefresh: Boolean,
    onRefreshTodayWord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastUpdatedTimeString = getTimeDiffString(anotherTime = lastUpdatedTime)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderTitle(titleTextStyle = titleTextStyle)
        HelpIcon(showTodayWordHelp = showTodayWordHelp)
        if (showUpdateInfo) {
            Spacer(modifier = Modifier.weight(1f))
            LastUpdateTimeText(lastUpdatedTimeString = lastUpdatedTimeString)
            RefreshIcon(
                onRefreshTodayWord = onRefreshTodayWord,
                enableRefresh = enableRefresh
            )
        }
    }
}

@Composable
private fun RefreshIcon(
    onRefreshTodayWord: () -> Unit,
    enableRefresh: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onRefreshTodayWord,
        modifier = modifier,
        enabled = enableRefresh
    ) {
        Icon(
            imageVector = Icons.Outlined.Autorenew,
            contentDescription = stringResource(R.string.refresh_today_word),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LastUpdateTimeText(
    lastUpdatedTimeString: String,
    modifier: Modifier = Modifier
) {
    MyVocaText(
        text = stringResource(R.string.last_updated, lastUpdatedTimeString),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun HelpIcon(
    showTodayWordHelp: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { showTodayWordHelp(true) },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Help,
            contentDescription = stringResource(R.string.what_is_today_word),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HeaderTitle(
    titleTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    MyVocaText(
        text = stringResource(id = R.string.today_word),
        modifier = modifier,
        style = titleTextStyle
    )
}

@Composable
private fun TodayWordContent(
    todayWord: HomeTodayWord,
    onTodayWordCheckboxChange: (HomeTodayWord) -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(targetValue = if (todayWord.todayWord.checked) 0.6f else 1f)
    val elevation by animateDpAsState(targetValue = if (todayWord.todayWord.checked) 0.dp else 6.dp)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Box {
            WordContent(
                word = todayWord.vocabulary,
                modifier = Modifier.alpha(alpha),
                showExpandButton = false,
                expanded = true,
                onExpanded = {}
            )
            Checkbox(
                checked = todayWord.todayWord.checked,
                modifier = Modifier.align(Alignment.TopEnd),
                onCheckedChange = { onTodayWordCheckboxChange(todayWord) },
            )
        }
    }
}

@Composable
private fun TodayWordTitle(
    modifier: Modifier = Modifier
) {
    MyVocaText(
        text = stringResource(R.string.no_registerd_word),
        modifier = modifier,
        maxLines = 2,
        style = MaterialTheme.typography.headlineMedium,
    )
}

@Composable
private fun TodayWordHelp(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onClose,
        modifier = modifier,
        title = {
            MyVocaText(
                text = stringResource(id = R.string.what_is_today_word),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MyVocaText(
                    text = stringResource(R.string.memorize_new_word_everyday_and_check_memorized_word),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                MyVocaText(
                    text = stringResource(R.string.words_are_update_everyday),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onClose) {
                    MyVocaText(
                        text = stringResource(R.string.check),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
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