package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.*
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

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = { AddWordTopBar(onAddWord = viewModel::onAddWord, onClose = onClose) }
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
    onAddWord: () -> Unit,
    onClose: () -> Unit
) {
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
            TextButton(onClick = {
                onAddWord()
                onClose()
            }) {
                Text(
                    text = "저장",
                    color = Color.White
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
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = word,
        label = { Text("단어") },
        onValueChange = onWordUpdate,
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
        AddWordMeanings(
            meanings = meanings,
            onMeaningUpdate = onMeaningUpdate,
            onMeaningDelete = onMeaningDelete
        )
    }
}

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
                modifier = Modifier.background(
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
            .sizeIn(maxHeight = 150.dp)
            .verticalScroll(scrollState)
    ) {
        meanings.forEach { meaning ->
            AddWordMeaning(
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

@Composable
private fun AddWordMeaning(
    meaning: MeaningImpl,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    // TODO: word class/content/delete 구현하기
    Text(
        text = meaning.type.korean
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddWordMemo(
    memo: String,
    onMemoUpdate: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = memo,
        label = { Text("메모") },
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
private fun AddWordWordPreview() {
    var word by remember { mutableStateOf("") }
    MyVocaTheme {
        AddWordAddWord(word = word, onWordUpdate = { word = it })
    }
}