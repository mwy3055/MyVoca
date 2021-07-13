package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsk.data.vocabulary.VocabularyQuery
import com.hsk.data.vocabulary.WordClass
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.data.toWordClass
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
        onOptionButtonClicked = allWordViewModel::onOptionButtonClicked,
        onSubmitButtonClicked = allWordViewModel::onSubmitButtonClicked,
        onCloseButtonClicked = allWordViewModel::onCloseButtonClicked,
        onQueryWordChanged = allWordViewModel::onQueryTextChanged,
        onOptionWordClassClick = allWordViewModel::onQueryWordClassToggled,
        onSortStateClick = allWordViewModel::onSortStateClicked
    )
}

@Composable
fun AllWordLoading(
    uiState: UiState<AllWordData>,
    onOptionButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit,
    onCloseButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1f)
            )
        }
        uiState.data?.let { data ->
            AllWordContent(
                data = data,
                onOptionButtonClicked = onOptionButtonClicked,
                onSubmitButtonClicked = onSubmitButtonClicked,
                onCloseButtonClicked = onCloseButtonClicked,
                onQueryWordChanged = onQueryWordChanged,
                onOptionWordClassClick = onOptionWordClassClick,
                onSortStateClick = onSortStateClick
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AllWordContent(
    data: AllWordData,
    onOptionButtonClicked: () -> Unit,
    onSubmitButtonClicked: () -> Unit,
    onCloseButtonClicked: () -> Unit,
    onQueryWordChanged: (String) -> Unit,
    onOptionWordClassClick: (String) -> Unit,
    onSortStateClick: (SortState) -> Unit
) {
    Column {
        AnimatedVisibility(data.optionVisible) {
            AllWordQueryOptions(
                modifier = Modifier
//                    .background(MaterialTheme.colors.primary)
                    .padding(8.dp),
                query = data.queryState,
                sortState = data.sortState,
                onSubmitButtonClicked = onSubmitButtonClicked,
                onCloseButtonClicked = onCloseButtonClicked,
                onQueryWordChanged = onQueryWordChanged,
                onOptionWordClassClick = onOptionWordClassClick,
                onSortStateClick = onSortStateClick
            )
        }
        AllWordHeader(
            vocabularySize = data.currentWordState.size,
            onOptionButtonClicked = onOptionButtonClicked
        )
        AllWordItems(data.currentWordState)
    }
}

@Composable
fun AllWordHeader(
    vocabularySize: Int,
    onOptionButtonClicked: () -> Unit
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
        TextButton(onClick = onOptionButtonClicked) {
            Icon(
                imageVector = Icons.Filled.ManageSearch,
                contentDescription = null
            )
            Text(text = "검색 옵션")
        }
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
    Box(
        modifier = modifier
            .fillMaxWidth()
//            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Buttons
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
    val classes = WordClassImpl.values().filter { it != WordClassImpl.UNKNOWN }
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LazyRow {
            item {
                WordClassChip(
                    className = totalWordClassName,
                    selected = selectedWordClass.isEmpty(),
                    onClick = onOptionWordClassClick
                )
            }
            items(classes) { wordClass ->
                WordClassChip(
                    className = wordClass.korean,
                    selected = selectedWordClass.contains(wordClass.toWordClass()),
                    onClick = onOptionWordClassClick
                )
            }
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

    Surface(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick(className) },
        elevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        color = background,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
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