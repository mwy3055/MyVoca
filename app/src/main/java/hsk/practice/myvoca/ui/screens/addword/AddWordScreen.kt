package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.components.StaggeredGrid
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel,
    onClose: () -> Unit = {}
) {
    val systemBarColor = MaterialTheme.colors.primaryVariant
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = systemBarColor
        )
    }

    val data by viewModel.addWordScreenData.collectAsState()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            AddWordTopBar(
                addButtonEnabled = data.canStoreWord,
                onAddWord = viewModel::onAddWord,
                onClose = onClose
            )
        }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            AddWordContent(
                data = data,
                onMeaningAdd = viewModel::onMeaningAdd,
                onWordUpdate = viewModel::onWordUpdate,
                onMeaningUpdate = viewModel::onMeaningUpdate,
                onMeaningDelete = viewModel::onMeaningDelete,
                onMemoUpdate = viewModel::onMemoUpdate
            )
        }
    }
}

@Composable
private fun AddWordTopBar(
    addButtonEnabled: Boolean,
    onAddWord: () -> Unit,
    onClose: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (addButtonEnabled) Color.White else Color.White.copy(alpha = 0.6f)
    )

    val focusManager = LocalFocusManager.current
    InsetAwareTopAppBar(
        title = {
            Text(text = "단어 추가")
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "단어 추가 화면을 닫습니다."
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        actions = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onAddWord()
                    onClose()
                },
                enabled = addButtonEnabled
            ) {
                Text(
                    text = "저장",
                    color = textColor
                )
            }
        }
    )
}

@Composable
private fun AddWordContent(
    data: AddWordScreenData,
    onWordUpdate: (String) -> Unit,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    onMemoUpdate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "필수 입력사항", style = MaterialTheme.typography.h5)
        AddWordAddWord(
            word = data.word,
            onWordUpdate = onWordUpdate
        )
        AddWordAddMeanings(
            meanings = data.meanings,
            onMeaningAdd = onMeaningAdd,
            onMeaningUpdate = onMeaningUpdate,
            onMeaningDelete = onMeaningDelete
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "선택 입력사항", style = MaterialTheme.typography.h5)
        AddWordMemo(
            memo = data.memo,
            onMemoUpdate = onMemoUpdate
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddWordAddWord(
    word: String,
    onWordUpdate: (String) -> Unit
) {
    // TODO: 단어가 이미 등록되어 있는지 아이콘으로 보여주기
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = word,
        label = { Text("단어") },
        onValueChange = onWordUpdate,
        colors = textFieldColors,
        trailingIcon = {
            AnimatedVisibility(visible = word.isNotEmpty()) {
                IconButton(onClick = { onWordUpdate("") }) {
                    Icon(
                        imageVector = Icons.Outlined.HighlightOff,
                        contentDescription = "영어 텍스트를 지웁니다."
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
private fun AddWordAddMeanings(
    meanings: List<MeaningImpl>,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    Column {
        AddWordMeaningChips(onMeaningAdd = onMeaningAdd)
        if (meanings.isEmpty()) {
            AddWordMeaningsEmpty()
        } else {
            AddWordMeanings(
                meanings = meanings,
                onMeaningUpdate = onMeaningUpdate,
                onMeaningDelete = onMeaningDelete
            )
        }
    }
}

private val meaningHeight = 210.dp

@Composable
private fun AddWordMeaningChips(
    onMeaningAdd: (WordClassImpl) -> Unit
) {
    StaggeredGrid {
        WordClassImpl.actualValues().forEach { wordClass ->
            AddWordWordClassChip(wordClass = wordClass, onMeaningAdd = onMeaningAdd)
        }
    }
}

@Composable
private fun AddWordWordClassChip(
    wordClass: WordClassImpl,
    onMeaningAdd: (WordClassImpl) -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onMeaningAdd(wordClass) }
            .padding(4.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colors.secondary.copy(alpha = 0.7f),
        border = BorderStroke(2.dp, MaterialTheme.colors.secondary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            IconButton(
                onClick = { onMeaningAdd(wordClass) },
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "$wordClass 뜻을 추가합니다."
                )
            }
            Text(
                text = wordClass.korean,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun AddWordMeaningsEmpty() {
    Box(
        modifier = Modifier
            .height(meaningHeight)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null
            )
            Text(
                text = "뜻을 추가해 보세요",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AddWordMeanings(
    meanings: List<MeaningImpl>,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    // TODO: make scroll bar. See: NestedScrollBar in Github star
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(meaningHeight)
            .verticalScroll(scrollState)
    ) {
        meanings.forEachIndexed { index, meaning ->
            AddWordMeaning(
                index = index,
                meaning = meaning,
                onMeaningUpdate = onMeaningUpdate,
                onMeaningDelete = onMeaningDelete
            )
        }
    }
}

fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 8.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = Color.Red,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddWordMeaning(
    index: Int,
    meaning: MeaningImpl,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = meaning.content,
            label = { Text(meaning.type.korean) },
            colors = textFieldColors,
            onValueChange = { onMeaningUpdate(index, meaning.copy(content = it)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        IconButton(onClick = { onMeaningDelete(index) }) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "${meaning.content} 뜻을 삭제합니다."
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddWordMemo(
    memo: String,
    onMemoUpdate: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = memo,
        label = { Text("메모") },
        colors = textFieldColors,
        onValueChange = onMemoUpdate,
        trailingIcon = {
            AnimatedVisibility(visible = memo.isNotEmpty()) {
                IconButton(onClick = { onMemoUpdate("") }) {
                    Icon(
                        imageVector = Icons.Outlined.HighlightOff,
                        contentDescription = "영어 텍스트를 지웁니다."
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Preview(showBackground = true)
@Composable
private fun AddWordScreenPreview() {
    val data = AddWordScreenData()
    MyVocaTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            AddWordContent(data = data,
                onWordUpdate = {},
                onMeaningAdd = {},
                onMeaningUpdate = { _, _ -> },
                onMeaningDelete = {},
                onMemoUpdate = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWordMeaningPreview() {
    val meaning = MeaningImpl(WordClassImpl.ADJECTIVE, "따뜻한")
    MyVocaTheme {
        AddWordMeaning(
            index = 0,
            meaning = meaning,
            onMeaningUpdate = { _, _ -> },
            onMeaningDelete = {}
        )
    }
}