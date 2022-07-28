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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsk.data.VocabularyQuery
import com.hsk.data.WordClass
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.ui.components.LoadingIndicator
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
    viewModel: AllWordViewModel = viewModel()
) {
    val uiState by viewModel.allWordUiState.collectAsState()

    Loading(
        uiState = uiState,
        onSubmitButtonClicked = viewModel::onSubmitButtonClicked,
        onQueryWordChanged = viewModel::onQueryTextChanged,
        onOptionWordClassClick = viewModel::onQueryWordClassToggled,
        onSortStateClick = viewModel::onSortStateClicked,
        onClearOption = viewModel::onClearOption,
        onWordUpdate = viewModel::onWordUpdate,
        onWordDelete = viewModel::onWordDelete
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
    onWordDelete: (VocabularyImpl) -> Unit = {}
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
                onWordDelete = onWordDelete
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
    onWordDelete: (VocabularyImpl) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)

    val revealBackdrop = {
        scope.launch { scaffoldState.reveal() }
    }
    val concealBackdrop = {
        scope.launch { scaffoldState.conceal() }
    }

    BackdropScaffold(
        appBar = {
            QueryHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BackdropScaffoldDefaults.HeaderHeight)
            )
        },
        backLayerContent = {
            QueryOptions(
                modifier = Modifier
                    .padding(8.dp),
                query = data.queryState,
                sortState = data.sortState,
                onSubmitButtonClicked = {
                    onSubmitButtonClicked()
                    concealBackdrop()
                },
                onCloseButtonClicked = {
                    onCloseButtonClicked()
                    concealBackdrop()
                },
                onQueryWordChanged = onQueryWordChanged,
                onOptionWordClassClick = onOptionWordClassClick,
                onSortStateClick = onSortStateClick
            )
        },
        backLayerBackgroundColor = MaterialTheme.colors.surface,
        frontLayerContent = {
            Column {
                Header(
                    vocabularySize = data.currentWordState.size,
                    showOptionClearButton = data.queryState != VocabularyQuery(),
                    onOptionButtonClicked = {
                        onOptionButtonClicked()
                        revealBackdrop()
                    },
                    onClearOption = onClearOption
                )
                WordItems(
                    words = data.currentWordState,
                    onWordUpdate = onWordUpdate,
                    onWordDelete = onWordDelete
                )
            }
            data.deletedWord?.let { deletedWord ->
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = "${deletedWord.eng}이(가) 삭제되었습니다.")
                }
            }
        },
        frontLayerElevation = 16.dp,
        scaffoldState = scaffoldState,
    )
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
            .background(MaterialTheme.colors.surface)
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
                contentDescription = "검색 옵션을 초기화합니다."
            )
            Text(text = "초기화")
        }
    }
}

@Composable
private fun ShowSearchOptionButton(onButtonClicked: () -> Unit) {
    TextButton(onClick = onButtonClicked) {
        Icon(
            imageVector = Icons.Filled.ManageSearch,
            contentDescription = "특정 조건에 맞는 단어를 검색합니다."
        )
        Text(text = "검색 옵션")
    }
}

@Composable
private fun HeaderText(
    vocabularySize: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "검색 결과: ${vocabularySize}개",
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
            contentDescription = "검색 옵션을 설정합니다."
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
        Text("닫기")
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
        Text("검색")
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
        label = { Text(text = "단어") },
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
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.onBackground),
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
    val background = MaterialTheme.colors.surface
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
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
                contentDescription = "$className 선택됨",
                tint = textColor
            )
        }
        Text(
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
        targetValue = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface
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
        Text(
            text = sortState.korean,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WordItems(
    words: ImmutableList<VocabularyImpl>,
    onWordUpdate: (VocabularyImpl, Context) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    Box {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(color = MaterialTheme.colors.background)
        ) {
            items(
                items = words,
                key = { it.id }
            ) { word ->
                WordContent(
                    modifier = Modifier.animateItemPlacement(
                        tween(
                            durationMillis = 1500,
                            easing = CubicBezierEasing(0.7f, 0.1f, 0.3f, 0.9f)
                        )
                    ),
                    word = word
                ) {
                    IconButton(onClick = { onWordUpdate(word, context) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "단어 ${word.eng}를 수정합니다."
                        )
                    }
                    IconButton(onClick = { onWordDelete(word) }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "단어 ${word.eng}를 삭제합니다."
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
    listState: LazyListState,
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
                color = MaterialTheme.colors.secondary,
                shape = CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowUpward,
                contentDescription = "목록의 맨 위로 이동합니다."
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
            onWordDelete = {})
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
                .background(MaterialTheme.colors.surface)
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
                .background(MaterialTheme.colors.surface)
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