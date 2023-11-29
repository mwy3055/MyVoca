@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.screens.allword

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import hsk.practice.myvoca.ui.components.WordEmptyIndicator
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
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
    resultLauncher: ActivityResultLauncher<Intent>,
    viewModel: AllWordViewModel = hiltViewModel()
) {
    val uiState by viewModel.allWordUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.data?.updateWord) {
        if (uiState.data?.updateWord != null) {
            val intent = Intent(context, AddWordActivity::class.java).apply {
                putExtra(AddWordActivity.updateWordId, uiState.data!!.updateWord!!.id)
            }
            resultLauncher.launch(intent)
            viewModel.onWordUpdate(null)
        }
    }

    Loading(
        uiState = uiState,
        onSubmitButtonClicked = viewModel::onSubmitButtonClicked,
        onQueryWordChanged = viewModel::onQueryTextChanged,
        onOptionWordClassClick = viewModel::onQueryWordClassToggled,
        onSortStateClick = viewModel::onSortStateClicked,
        onClearButtonClicked = viewModel::onClearOption,
        onWordUpdate = viewModel::onWordUpdate,
        onWordDelete = viewModel::onWordDelete,
        onWordRestore = viewModel::onWordRestore,
        onWordDeleteCompleteUpdate = viewModel::onWordDeleteCompleteUpdate
    )
}

@Composable
private fun Loading(
    uiState: UiState<AllWordData>,
    onSubmitButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit,
    onClearButtonClicked: () -> Unit,
    onWordUpdate: (VocabularyImpl) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit,
    onWordRestore: (VocabularyImpl) -> Unit,
    onWordDeleteCompleteUpdate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        if (uiState.loading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.data?.let { data ->
            Content(
                data = data,
                onSubmitButtonClicked = onSubmitButtonClicked,
                onQueryWordChanged = onQueryWordChanged,
                onOptionWordClassClick = onOptionWordClassClick,
                onSortStateClick = onSortStateClick,
                onClearButtonClicked = onClearButtonClicked,
                onWordUpdate = onWordUpdate,
                onWordDelete = onWordDelete,
                onWordRestore = onWordRestore,
                onWordDeleteCompleteUpdate = onWordDeleteCompleteUpdate,
            )
        }
    }
}

@Composable
private fun Content(
    data: AllWordData,
    onSubmitButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit,
    onClearButtonClicked: () -> Unit,
    onWordUpdate: (VocabularyImpl) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit,
    onWordRestore: (VocabularyImpl) -> Unit,
    onWordDeleteCompleteUpdate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(key1 = data.deleteWordComplete) {
        if (data.deleteWordComplete) {
            data.deletedWord?.let {
                val snackbarResult = scaffoldState.snackbarHostState
                    .showSnackbar(
                        message = context.getString(
                            R.string.delete_word_complete,
                            it.eng
                        ),
                        actionLabel = context.getString(R.string.undo),
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
        onWordDeleteCompleteUpdate(false)
    }

    BottomSheetScaffold(
        sheetContent = {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                QueryOptions(
                    query = data.queryState,
                    sortState = data.sortState,
                    onQueryWordChanged = onQueryWordChanged,
                    onOptionWordClassClick = onOptionWordClassClick,
                    onSortStateClick = onSortStateClick,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetShadowElevation = 36.dp,
        sheetDragHandle = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Header(
                    vocabularySize = data.currentWordState.size,
                    queryState = data.queryState,
                    submitState = data.submitState,
                    bottomSheetState = scaffoldState.bottomSheetState,
                    onClearButtonClicked = onClearButtonClicked,
                    onSubmitButtonClicked = onSubmitButtonClicked,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (data.currentWordState.isEmpty()) {
                WordEmptyIndicator()
            } else {
                AllWords(
                    modifier = Modifier.fillMaxSize(),
                    words = data.currentWordState,
                    onWordUpdate = onWordUpdate,
                    onWordDelete = onWordDelete
                )
            }
        }
    }
}

@Composable
private fun Header(
    vocabularySize: Int,
    queryState: VocabularyQuery,
    submitState: Boolean,
    bottomSheetState: SheetState,
    onClearButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText(
            vocabularySize = vocabularySize,
            submitState = submitState,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(
            visible = queryState != VocabularyQuery(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SearchOptionClearButton(
                onButtonClicked = onClearButtonClicked
            )
        }
        ShowSearchOptionButton(
            bottomSheetState = bottomSheetState,
            onButtonClicked = onSubmitButtonClicked
        )
    }
}

@Composable
private fun SearchOptionClearButton(
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onButtonClicked,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = stringResource(R.string.initialize_search_options),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        MyVocaText(
            text = stringResource(R.string.initialization),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ShowSearchOptionButton(
    bottomSheetState: SheetState,
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    TextButton(
        onClick = {
            onButtonClicked()
            if (bottomSheetState.hasExpandedState) {
                coroutineScope.launch {
                    bottomSheetState.partialExpand()
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.search_for_word_that_matchs_particular_condition),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        MyVocaText(
            text = stringResource(R.string.search_options),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun HeaderText(
    vocabularySize: Int,
    submitState: Boolean,
    modifier: Modifier = Modifier
) {
    MyVocaText(
        text = if (submitState) stringResource(R.string.search_results_count, vocabularySize)
        else stringResource(R.string.total_words_count, vocabularySize),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun QueryHeader(
    modifier: Modifier = Modifier
) {
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
    query: VocabularyQuery,
    sortState: SortState,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QueryWord(
            text = query.word,
            onTextChanged = onQueryWordChanged,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(MaterialTheme.colorScheme.onPrimaryContainer)
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
    modifier: Modifier = Modifier,
    focusManager: FocusManager = LocalFocusManager.current
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChanged,
        modifier = modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
private fun QueryWordClass(
    selectedWordClass: ImmutableSet<WordClass>,
    onOptionWordClassClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier.padding(vertical = 8.dp)) {
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
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondaryContainer
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    )

    Row(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(color = background, shape = MaterialTheme.shapes.large)
            .clip(RoundedCornerShape(percent = 50))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large
            )
            .clickable { onClick(className) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.selected, className),
                modifier = Modifier.padding(start = 4.dp),
                tint = textColor
            )
        }
        MyVocaText(
            text = className,
            modifier = Modifier.padding(
                start = if (selected) 4.dp else 8.dp,
                end = 8.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
            color = textColor
        )
    }
}

@Composable
private fun QuerySortState(
    currentSortState: SortState,
    onSortStateClick: (SortState) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortStates = SortState.values()
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.extraLarge
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sortStates.forEach { sortState ->
            SortStateChip(
                modifier = Modifier.weight(1f),
                sortState = sortState,
                selected = (sortState == currentSortState),
                onClick = onSortStateClick
            )
        }
    }
}

@Composable
private fun SortStateChip(
    sortState: SortState,
    selected: Boolean,
    onClick: (SortState) -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondaryContainer
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .clickable { onClick(sortState) },
        contentAlignment = Alignment.Center
    ) {
        MyVocaText(
            text = sortState.korean,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllWords(
    words: ImmutableList<VocabularyImpl>,
    onWordUpdate: (VocabularyImpl) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyGridState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LazyVerticalGrid(
        modifier = modifier,
        state = listState,
        columns = GridCells.Adaptive(minSize = min(screenWidth, 330.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (words.isEmpty()) {
            item {
                WordEmptyIndicator()
            }
        } else {
            items(
                items = words,
                key = { it.id },
            ) { word ->
                Card (
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    WordContent(
                        word = word,
                        modifier = Modifier.animateItemPlacement(
                            tween(
                                durationMillis = 500,
                                easing = CubicBezierEasing(0.7f, 0.1f, 0.3f, 0.9f)
                            )
                        )
                    ) {
                        IconButton(
                            onClick = { onWordUpdate(word) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(
                                    R.string.update_the_word,
                                    word.eng
                                )
                            )
                        }
                        IconButton(onClick = { onWordDelete(word) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_delete_outline_24),
                                contentDescription = stringResource(
                                    R.string.delete_the_word,
                                    word.eng
                                )
                            )
                        }
                    }
                }
            }
        }
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
            onSubmitButtonClicked = { },
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
            onClearButtonClicked = {},
            onWordUpdate = {},
            onWordDelete = {},
            onWordRestore = {},
            onWordDeleteCompleteUpdate = { _ -> }
        )
    }
}

@Preview
@Composable
private fun WordItemsPreview() {
    MyVocaTheme {
        AllWords(
            words = fakeData.toImmutableList(),
            onWordUpdate = {},
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