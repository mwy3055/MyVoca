@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.screens.allword

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsk.data.VocabularyQuery
import com.hsk.data.WordClass
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.state.UiState
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AllWordScreen(
    viewModel: AllWordViewModel = hiltViewModel()
) {
    val uiState by viewModel.allWordUiState.collectAsStateWithLifecycle()

    Loading(
        uiState = uiState,
        onSubmitButtonClicked = viewModel::onSubmitButtonClicked,
        onQueryWordChanged = viewModel::onQueryTextChanged,
        onOptionWordClassClick = viewModel::onQueryWordClassToggled,
        onSortStateClick = viewModel::onSortStateClicked,
        onClearOption = viewModel::onClearOption,
        onWordUpdate = viewModel::onWordUpdate,
        onWordDelete = viewModel::onWordDelete,
        onWordRestore = viewModel::onWordRestore
    )
}

@Composable
private fun Loading(
    uiState: UiState<AllWordData>,
    onOptionButtonClicked: () -> Unit = {},
    onSubmitButtonClicked: () -> Unit = {},
    onCloseButtonClicked: () -> Unit = {},
    onQueryWordChanged: (String) -> Unit = {},
    onOptionWordClassClick: (String) -> Unit = {},
    onSortStateClick: (SortState) -> Unit = {},
    onClearOption: () -> Unit = {},
    onWordUpdate: (VocabularyImpl, Context) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit = {},
    onWordRestore: (VocabularyImpl) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.loading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.data?.let { data ->
            Content(
                data = data,
                onOptionButtonClicked = onOptionButtonClicked,
                onSubmitButtonClicked = onSubmitButtonClicked,
                onCloseButtonClicked = onCloseButtonClicked,
                onQueryWordChanged = onQueryWordChanged,
                onOptionWordClassClick = onOptionWordClassClick,
                onSortStateClick = onSortStateClick,
                onClearOption = onClearOption,
                onWordUpdate = onWordUpdate,
                onWordDelete = onWordDelete,
                onWordRestore = onWordRestore,
            )
        }
    }
}

@Composable
private fun Content(
    data: AllWordData,
    onOptionButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit,
    onCloseButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit,
    onClearOption: () -> Unit,
    onWordUpdate: (VocabularyImpl, Context) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit,
    onWordRestore: (VocabularyImpl) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(key1 = data.deletedWord) {
        data.deletedWord?.let {
            val snackbarResult = scaffoldState.snackbarHostState
                .showSnackbar(
                    message = "${data.deletedWord.eng}이(가) 삭제되었습니다.",
                    actionLabel = "실행 취소",
                    duration = SnackbarDuration.Short
                )
            when (snackbarResult) {
                SnackbarResult.ActionPerformed -> {
                    onWordRestore(data.deletedWord)
                }

                SnackbarResult.Dismissed -> Unit
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box {
                Column {
                    Header(
                        vocabularySize = data.currentWordState.size,
                        showOptionClearButton = data.queryState != VocabularyQuery(),
                        onOptionButtonClicked = {
                            onOptionButtonClicked()
                        },
                        onClearOption = onClearOption
                    )
                    QueryOptions(
                        modifier = Modifier
                            .padding(8.dp),
                        query = data.queryState,
                        sortState = data.sortState,
                        onSubmitButtonClicked = {
                            onSubmitButtonClicked()
                        },
                        onCloseButtonClicked = {
                            onCloseButtonClicked()
                        },
                        onQueryWordChanged = onQueryWordChanged,
                        onOptionWordClassClick = onOptionWordClassClick,
                        onSortStateClick = onSortStateClick
                    )
                }
            }
        }
    ) { innerPadding ->
        WordItems(
            modifier = Modifier.padding(innerPadding),
            words = data.currentWordState,
            onWordUpdate = onWordUpdate,
            onWordDelete = onWordDelete
        )
    }
}

@Composable
private fun Header(
    vocabularySize: Int,
    showOptionClearButton: Boolean,
    onOptionButtonClicked: () -> Unit,
    onClearOption: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText(
            vocabularySize = vocabularySize,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        SearchOptionClearButton(
            showOptionClearButton = showOptionClearButton,
            onClearOption = onClearOption
        )
        ShowSearchOptionButton(onButtonClicked = onOptionButtonClicked)
    }
}

@Composable
private fun SearchOptionClearButton(
    showOptionClearButton: Boolean,
    onClearOption: () -> Unit
) {
    if (showOptionClearButton) {
        TextButton(onClick = onClearOption) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.initialize_search_options)
            )
            MyVocaText(text = stringResource(R.string.initialization))
        }
    }
}

@Composable
private fun ShowSearchOptionButton(onButtonClicked: () -> Unit) {
    TextButton(onClick = onButtonClicked) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ManageSearch,
            contentDescription = stringResource(R.string.search_for_word_that_matchs_particular_condition)
        )
        MyVocaText(text = stringResource(R.string.search_options))
    }
}

@Composable
private fun HeaderText(
    vocabularySize: Int,
    modifier: Modifier = Modifier
) {
    MyVocaText(
        text = stringResource(R.string.search_results_count, vocabularySize),
        modifier = modifier
    )
}

@Composable
private fun QueryHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = stringResource(R.string.set_search_options)
        )
    }
}

@Composable
private fun QueryOptions(
    modifier: Modifier = Modifier,
    query: VocabularyQuery,
    sortState: SortState,
    onSubmitButtonClicked: () -> Unit,
    onCloseButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QueryActions(
            onCloseButtonClicked = onCloseButtonClicked,
            onSubmitButtonClicked = onSubmitButtonClicked
        )
        QueryWord(
            text = query.word,
            onTextChanged = onQueryWordChanged
        )
        QueryWordClass(
            selectedWordClass = query.wordClass,
            onOptionWordClassClick = onOptionWordClassClick
        )
        QuerySortState(
            currentSortState = sortState,
            onSortStateClick = onSortStateClick
        )
    }
}

@Composable
private fun QueryActions(
    onCloseButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val closeKeyboard = { focusManager.clearFocus() }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CloseQueryButton(
            onCloseButtonClicked = onCloseButtonClicked,
            closeKeyboard = closeKeyboard,
            modifier = Modifier.weight(1f)
        )
        SubmitQueryButton(
            onSubmitButtonClicked = onSubmitButtonClicked,
            closeKeyboard = closeKeyboard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CloseQueryButton(
    onCloseButtonClicked: () -> Unit,
    closeKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = {
            onCloseButtonClicked()
            closeKeyboard()
        },
        modifier = modifier
    ) {
        MyVocaText(text = stringResource(id = R.string.close))
    }
}

@Composable
private fun SubmitQueryButton(
    onSubmitButtonClicked: () -> Unit,
    closeKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            onSubmitButtonClicked()
            closeKeyboard()
        },
        modifier = modifier
    ) {
        MyVocaText(text = stringResource(R.string.search))
    }
}

@Composable
private fun QueryWord(
    text: String,
    onTextChanged: (String) -> Unit,
    focusManager: FocusManager = LocalFocusManager.current
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = text,
        label = { MyVocaText(text = stringResource(id = R.string.word)) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onTextChanged("") }) {
                    Icon(
                        imageVector = Icons.Outlined.HighlightOff,
                        contentDescription = null
                    )
                }
            }
        },
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        singleLine = true,
        onValueChange = onTextChanged,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
private fun QueryWordClass(
    selectedWordClass: ImmutableSet<WordClass>,
    onOptionWordClassClick: (String) -> Unit
) {
    LazyRow(modifier = Modifier.padding(8.dp)) {
        item {
            WordClassChip(
                className = totalWordClassName,
                selected = selectedWordClass.isEmpty(),
                onClick = onOptionWordClassClick
            )
        }
        items(WordClassImpl.actualValues()) { wordClass ->
            WordClassChip(
                className = wordClass.korean,
                selected = selectedWordClass.contains(wordClass.toWordClass()),
                onClick = onOptionWordClassClick
            )
        }
    }
}

@Composable
private fun WordClassChip(
    className: String,
    selected: Boolean,
    onClick: (String) -> Unit,
) {
    val background = MaterialTheme.colorScheme.surface
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick(className) }
            .background(color = background, shape = MaterialTheme.shapes.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.selected, className),
                tint = textColor
            )
        }
        MyVocaText(
            text = className,
            modifier = Modifier.padding(6.dp),
            color = textColor
        )
    }
}

@Composable
private fun QuerySortState(
    currentSortState: SortState,
    onSortStateClick: (SortState) -> Unit
) {
    val sortStates = SortState.values()
    Row(
        modifier = Modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sortStates.forEach { sortState ->
            SortStateChip(
                modifier = Modifier
                    .weight(1f),
                sortState = sortState,
                selected = (sortState == currentSortState),
                onClick = onSortStateClick
            )
        }
    }
}

@Composable
private fun SortStateChip(
    modifier: Modifier = Modifier,
    sortState: SortState,
    selected: Boolean,
    onClick: (SortState) -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    )
    val textColor by animateColorAsState(
        targetValue = contentColorFor(background)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .clickable { onClick(sortState) },
        contentAlignment = Alignment.Center,
    ) {
        MyVocaText(
            text = sortState.korean,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WordItems(
    modifier: Modifier = Modifier,
    words: ImmutableList<VocabularyImpl>,
    onWordUpdate: (VocabularyImpl, Context) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyGridState()
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Box {
        LazyVerticalGrid(
            modifier = modifier.background(color = MaterialTheme.colorScheme.background),
            state = listState,
            columns = GridCells.Adaptive(minSize = min(screenWidth, 330.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = words.size,
                key = { words[it].id },
            ) {
                WordContent(
                    modifier = Modifier.animateItemPlacement(
                        tween(
                            durationMillis = 500,
                            easing = CubicBezierEasing(0.7f, 0.1f, 0.3f, 0.9f)
                        )
                    ),
                    word = words[it]
                ) {
                    IconButton(onClick = { onWordUpdate(words[it], context) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(
                                R.string.update_the_word,
                                words[it].eng
                            )
                        )
                    }
                    IconButton(onClick = { onWordDelete(words[it]) }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(
                                R.string.delete_the_word,
                                words[it].eng
                            )
                        )
                    }
                }
            }
        }
        ScrollTopButton(
            listState = listState,
            coroutineScope = coroutineScope,
            modifier = Modifier.Companion
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ScrollTopButton(
    listState: LazyGridState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = showButton,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { with(density) { -40.dp.roundToPx() } },
        exit = fadeOut() + slideOutVertically { with(density) { -40.dp.roundToPx() } },
    ) {
        IconButton(
            onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowUpward,
                contentDescription = stringResource(R.string.go_to_the_top_of_the_list)
            )
        }
    }
}

@Preview
@Composable
private fun ContentsPreview() {
    var word = "test text"
    val wordClassSet = mutableSetOf<WordClass>()

    MyVocaTheme {
        Content(
            data = AllWordData(
                currentWordState = fakeData.toImmutableList(),
                queryState = VocabularyQuery(word = word)
            ),
            onOptionButtonClicked = { },
            onSubmitButtonClicked = { },
            onCloseButtonClicked = { },
            onQueryWordChanged = { word = it },
            onOptionWordClassClick = { wordClassName ->
                val wordClass = WordClassImpl.findByKorean(wordClassName)?.toWordClass()
                    ?: return@Content
                if (wordClassSet.contains(wordClass)) {
                    wordClassSet.remove(wordClass)
                } else {
                    wordClassSet.add(wordClass)
                }
            },
            onSortStateClick = {},
            onClearOption = {},
            onWordUpdate = { _, _ -> },
            onWordDelete = {},
            onWordRestore = {}
        )
    }
}

@Preview
@Composable
private fun WordItemsPreview() {
    MyVocaTheme {
        WordItems(
            words = fakeData.toImmutableList(),
            onWordUpdate = { _, _ -> },
            onWordDelete = {}
        )
    }
}

@Preview
@Composable
private fun QueryOptionsPreview() {
    var word = "test text"
    val wordClassSet = persistentSetOf<WordClass>()

    MyVocaTheme {
        QueryOptions(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            query = VocabularyQuery(
                word = word,
                wordClass = wordClassSet
            ),
            sortState = SortState.Alphabet,
            onSubmitButtonClicked = {},
            onCloseButtonClicked = {},
            onQueryWordChanged = { word = it },
            onSortStateClick = {},
            onOptionWordClassClick = { wordClassName ->
                val wordClass = WordClassImpl.findByKorean(wordClassName)?.toWordClass()
                    ?: return@QueryOptions
                if (wordClassSet.contains(wordClass)) {
                    wordClassSet.remove(wordClass)
                } else {
                    wordClassSet.add(wordClass)
                }
            }
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun QueryOptionsPreview_DarkMode() {
    var word = "test text"
    val wordClassSet = persistentSetOf<WordClass>()

    MyVocaTheme {
        QueryOptions(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            query = VocabularyQuery(
                word = word,
                wordClass = wordClassSet
            ),
            sortState = SortState.Alphabet,
            onSubmitButtonClicked = {},
            onCloseButtonClicked = {},
            onQueryWordChanged = { word = it },
            onSortStateClick = {},
            onOptionWordClassClick = { wordClassName ->
                val wordClass = WordClassImpl.findByKorean(wordClassName)?.toWordClass()
                    ?: return@QueryOptions
                if (wordClassSet.contains(wordClass)) {
                    wordClassSet.remove(wordClass)
                } else {
                    wordClassSet.add(wordClass)
                }
            }
        )
    }
}