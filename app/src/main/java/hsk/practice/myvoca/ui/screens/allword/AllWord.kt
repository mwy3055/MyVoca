package hsk.practice.myvoca.ui.screens.allword

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.data.vocabulary.WordClass
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.data.toWordClass
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.state.UiState
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.coroutines.launch

@Composable
fun AllWordScreen(
    viewModel: AllWordViewModel = viewModel()
) {
    val allWordUiState by viewModel.allWordUiState.collectAsState()

    AllWordLoading(
        uiState = allWordUiState,
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
fun AllWordLoading(
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
            AllWordContent(
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
fun AllWordContent(
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
            AllWordQueryIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BackdropScaffoldDefaults.HeaderHeight)
            )
        },
        backLayerContent = {
            AllWordQueryOptions(
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
                AllWordHeader(
                    vocabularySize = data.currentWordState.size,
                    showOptionClearButton = data.queryState != VocabularyQuery(),
                    onOptionButtonClicked = {
                        onOptionButtonClicked()
                        revealBackdrop()
                    },
                    onClearOption = onClearOption
                )
                AllWordItems(
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
fun AllWordHeader(
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
        Text(
            text = "검색 결과: ${vocabularySize}개",
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showOptionClearButton) {
            TextButton(onClick = onClearOption) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "검색 옵션을 초기화합니다."
                )
                Text(text = "초기화")
            }

        }
        TextButton(onClick = onOptionButtonClicked) {
            Icon(
                imageVector = Icons.Filled.ManageSearch,
                contentDescription = "특정 조건에 맞는 단어를 검색합니다."
            )
            Text(text = "검색 옵션")
        }
    }
}

@Composable
fun AllWordQueryIndicator(modifier: Modifier = Modifier) {
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
fun AllWordQueryOptions(
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
        AllWordQueryButtons(
            onCloseButtonClicked = onCloseButtonClicked,
            onSubmitButtonClicked = onSubmitButtonClicked
        )
        AllWordQueryWord(
            text = query.word,
            onTextChanged = onQueryWordChanged
        )
        AllWordQueryWordClass(
            selectedWordClass = query.wordClass,
            onOptionWordClassClick = onOptionWordClassClick
        )
        AllWordQuerySortState(
            currentSortState = sortState,
            onSortStateClick = onSortStateClick
        )
    }
}

@Composable
fun AllWordQueryButtons(
    onCloseButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val closeKeyboard = { focusManager.clearFocus() }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            onClick = {
                onCloseButtonClicked()
                closeKeyboard()
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("닫기")
        }
        Button(
            onClick = {
                onSubmitButtonClicked()
                closeKeyboard()
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("검색")
        }
    }
}

@Composable
fun AllWordQueryWord(
    focusManager: FocusManager = LocalFocusManager.current,
    text: String,
    onTextChanged: (String) -> Unit
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
        singleLine = true,
        onValueChange = onTextChanged,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
fun AllWordQueryWordClass(
    selectedWordClass: Set<WordClass>,
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WordClassChip(
    className: String,
    selected: Boolean,
    onClick: (String) -> Unit,
) {
    val background by animateColorAsState(targetValue = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface)
    val textColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary)

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
                contentDescription = null,
                tint = contentColorFor(backgroundColor = MaterialTheme.colors.primary)
            )
        }
        Text(
            text = className,
            modifier = Modifier
                .padding(6.dp),
            color = textColor
        )
    }
}

@Composable
fun AllWordQuerySortState(
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
fun SortStateChip(
    modifier: Modifier = Modifier,
    sortState: SortState,
    selected: Boolean,
    onClick: (SortState) -> Unit
) {
    val background by animateColorAsState(targetValue = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface)
    val textColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary)

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
fun AllWordItems(
    words: List<VocabularyImpl>,
    onWordUpdate: (VocabularyImpl, Context) -> Unit,
    onWordDelete: (VocabularyImpl) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    Box {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

        val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
        if (showButton) {
            IconButton(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(color = MaterialTheme.colors.secondary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = "목록의 맨 위로 이동합니다."
                )
            }
        }
    }
}

@Preview
@Composable
fun AllWordContentsPreview() {
    var word = "test text"
    val wordClassSet = mutableSetOf<WordClass>()

    MyVocaTheme {
        AllWordContent(
            data = AllWordData(
                currentWordState = fakeData,
                queryState = VocabularyQuery(word = word)
            ),
            onOptionButtonClicked = { },
            onSubmitButtonClicked = { },
            onCloseButtonClicked = { },
            onQueryWordChanged = { word = it },
            onOptionWordClassClick = { wordClassName ->
                val wordClass = WordClassImpl.findByKorean(wordClassName)?.toWordClass()
                    ?: return@AllWordContent
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
fun AllWordItemsPreview() {
    MyVocaTheme {
        AllWordItems(
            words = fakeData,
            onWordUpdate = { _, _ -> },
            onWordDelete = {}
        )
    }
}

@Preview
@Composable
fun AllWordQueryOptionsPreview() {
    var word = "test text"
    val wordClassSet = mutableSetOf<WordClass>()

    MyVocaTheme {
        AllWordQueryOptions(
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
                    ?: return@AllWordQueryOptions
                if (wordClassSet.contains(wordClass)) {
                    wordClassSet.remove(wordClass)
                } else {
                    wordClassSet.add(wordClass)
                }
            }
        )
    }
}