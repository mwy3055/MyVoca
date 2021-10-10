package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.components.StaggeredGrid
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AddWordScreen(
    modifier: Modifier = Modifier,
    viewModel: AddWordViewModel,
    updateWordId: Int,
    onClose: () -> Unit = {}
) {
    val systemBarColor = MaterialTheme.colors.primaryVariant
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = systemBarColor
        )
    }

    LaunchedEffect(key1 = true) {
        if (updateWordId != -1) viewModel.injectUpdateWord(updateWordId)
    }

    val data by viewModel.addWordScreenData.collectAsState()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier,
        topBar = {
            AddWordTopBar(
                screenType = data.screenType,
                addButtonEnabled = data.canStoreWord,
                onAddWord = viewModel::onAddWord,
                onClose = onClose
            )
        }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            AddWordContent(
                data = data,
                loadStatus = viewModel::loadStatus,
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
    screenType: ScreenType,
    addButtonEnabled: Boolean,
    onAddWord: () -> Unit,
    onClose: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (addButtonEnabled) Color.White else Color.White.copy(alpha = 0.6f)
    )

    val title = when (screenType) {
        AddWord -> "단어 추가"
        UpdateWord -> "단어 수정"
    }

    val focusManager = LocalFocusManager.current
    InsetAwareTopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "$title 화면을 닫습니다."
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
    loadStatus: suspend (String) -> Unit,
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
            status = data.wordExistStatus,
            loadStatus = loadStatus,
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

@Composable
private fun AddWordAddWord(
    word: String,
    status: WordExistStatus,
    loadStatus: suspend (String) -> Unit,
    onWordUpdate: (String) -> Unit
) {
    LaunchedEffect(word) {
        loadStatus(word)
    }

    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)

    val statusIcon = getWordStatusIcon(status)
    val iconColor = getWordStatusIconColor(status)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = word,
            label = { Text("단어") },
            onValueChange = onWordUpdate,
            colors = textFieldColors,
            trailingIcon = {
                statusIcon?.let {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = iconColor
                    )
                }
            },
            singleLine = true,
            isError = (status == WordExistStatus.DUPLICATE),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        Text(
            text = "이미 등록된 단어입니다.",
            style = MaterialTheme.typography.caption,
            color = if (status == WordExistStatus.DUPLICATE) iconColor else Color.Transparent
        )
    }
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
            label = { Text("${index + 1}. ${meaning.type.korean}") },
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

@Composable
private fun getWordStatusIcon(status: WordExistStatus): ImageVector? {
    return when (status) {
        WordExistStatus.NOT_EXISTS -> Icons.Outlined.CheckCircle
        WordExistStatus.DUPLICATE -> Icons.Outlined.ErrorOutline
        WordExistStatus.LOADING -> Icons.Outlined.HourglassFull
        WordExistStatus.WORD_EMPTY -> null
    }
}

@Composable
private fun getWordStatusIconColor(status: WordExistStatus): Color {
    return when (status) {
        WordExistStatus.NOT_EXISTS -> MaterialTheme.colors.primary
        WordExistStatus.DUPLICATE -> MaterialTheme.colors.error
        else -> LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWordScreenPreview() {
    val data = AddWordScreenData()
    MyVocaTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            AddWordContent(data = data,
                loadStatus = {},
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