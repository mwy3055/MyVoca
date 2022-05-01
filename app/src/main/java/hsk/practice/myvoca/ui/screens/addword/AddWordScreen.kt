package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.components.StaggeredGrid
import hsk.practice.myvoca.ui.components.SystemBarColor
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AddWordScreen(
    modifier: Modifier = Modifier,
    viewModel: AddWordViewModel,
    updateWordId: Int,
    onClose: () -> Unit = {}
) {
    SystemBarColor(systemBarColor = MaterialTheme.colors.primaryVariant)
    LaunchedEffect(key1 = true) {
        if (updateWordId != -1) viewModel.injectUpdateTarget(updateWordId)
    }

    val uiState by viewModel.uiStateFlow.collectAsState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier,
        topBar = {
            TopBar(
                screenType = uiState.screenType,
                addButtonEnabled = uiState.canStoreWord,
                onAddWord = viewModel::onAddWord,
                onClose = onClose
            )
        }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Content(
                data = uiState,
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
private fun TopBar(
    screenType: ScreenType,
    addButtonEnabled: Boolean,
    onAddWord: () -> Unit,
    onClose: () -> Unit
) {
    val textAlpha by animateFloatAsState(targetValue = if (addButtonEnabled) 1f else 0.6f)
    InsetAwareTopAppBar(
        title = {
            TopBarTitle(screenType)
        },
        navigationIcon = {
            TopBarCloseButton(onClose = onClose)
        },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        actions = {
            TopBarSaveButton(
                onAddWord = onAddWord,
                onClose = onClose,
                addButtonEnabled = addButtonEnabled,
                textColor = Color.White.copy(alpha = textAlpha)
            )
        }
    )
}

@Composable
private fun TopBarTitle(screenType: ScreenType) {
    val title = when (screenType) {
        AddWord -> "단어 추가"
        UpdateWord -> "단어 수정"
    }
    Text(text = title)
}

@Composable
private fun TopBarCloseButton(onClose: () -> Unit) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "화면을 닫습니다."
        )
    }
}

@Composable
private fun TopBarSaveButton(
    onAddWord: () -> Unit,
    onClose: () -> Unit,
    addButtonEnabled: Boolean,
    textColor: Color,
    focusManager: FocusManager = LocalFocusManager.current,
) {
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

@Composable
private fun Content(
    data: AddWordScreenData,
    loadStatus: suspend (String) -> Unit,
    onWordUpdate: (String) -> Unit,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    onMemoUpdate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EssentialTitle()
        Word(
            word = data.word,
            status = data.wordExistStatus,
            loadStatus = loadStatus,
            onWordUpdate = onWordUpdate
        )
        Meanings(
            meanings = data.meanings,
            onMeaningAdd = onMeaningAdd,
            onMeaningUpdate = onMeaningUpdate,
            onMeaningDelete = onMeaningDelete
        )
        Spacer(modifier = Modifier.height(10.dp))
        OptionalTitle()
        Memo(
            memo = data.memo,
            onMemoUpdate = onMemoUpdate
        )
    }
}

@Composable
private fun EssentialTitle() {
    Text(text = "필수 입력사항", style = MaterialTheme.typography.h5)
}

@Composable
private fun OptionalTitle() {
    Text(text = "선택 입력사항", style = MaterialTheme.typography.h5)
}

@Composable
private fun Word(
    word: String,
    status: WordExistStatus,
    loadStatus: suspend (String) -> Unit,
    onWordUpdate: (String) -> Unit,
    focusManager: FocusManager = LocalFocusManager.current
) {
    LaunchedEffect(word) {
        loadStatus(word)
    }

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
        val duplicateTextColor by animateColorAsState(
            targetValue = if (status == WordExistStatus.DUPLICATE) iconColor else Color.Transparent,
            animationSpec = tween(
                durationMillis = 300,
            )
        )
        Text(
            text = "이미 등록된 단어입니다.",
            style = MaterialTheme.typography.caption,
            color = duplicateTextColor
        )
    }
}

@Composable
private fun Meanings(
    meanings: List<MeaningImpl>,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    Column {
        MeaningChips(onMeaningAdd = onMeaningAdd)
        if (meanings.isEmpty()) {
            MeaningsEmptyIndicator()
        } else {
            MeaningsContent(
                meanings = meanings,
                onMeaningUpdate = onMeaningUpdate,
                onMeaningDelete = onMeaningDelete
            )
        }
    }
}

private val meaningHeight = 210.dp

@Composable
private fun MeaningChips(
    onMeaningAdd: (WordClassImpl) -> Unit
) {
    StaggeredGrid {
        WordClassImpl.actualValues().forEach { wordClass ->
            WordClassChip(wordClass = wordClass, onMeaningAdd = onMeaningAdd)
        }
    }
}

@Composable
private fun WordClassChip(
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
            WordClassChipIcon(
                onMeaningAdd = onMeaningAdd,
                wordClass = wordClass
            )
            WordClassChipText(wordClass = wordClass)
        }
    }
}

@Composable
private fun WordClassChipIcon(
    onMeaningAdd: (WordClassImpl) -> Unit,
    wordClass: WordClassImpl
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
}

@Composable
private fun WordClassChipText(wordClass: WordClassImpl) {
    Text(
        text = wordClass.korean,
        modifier = Modifier.padding(end = 4.dp)
    )
}

@Composable
private fun MeaningsEmptyIndicator() {
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
private fun MeaningsContent(
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
            Meaning(
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
private fun Meaning(
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
            label = {
                Text(text = "${index + 1}. ${meaning.type.korean}")
            },
            colors = textFieldColors,
            onValueChange = {
                onMeaningUpdate(index, meaning.copy(content = it))
            },
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
private fun Memo(
    memo: String,
    onMemoUpdate: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = memo,
        label = {
            Text("메모")
        },
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
            Content(data = data,
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
private fun MeaningPreview() {
    val meaning = MeaningImpl(WordClassImpl.ADJECTIVE, "따뜻한")
    MyVocaTheme {
        Meaning(
            index = 0,
            meaning = meaning,
            onMeaningUpdate = { _, _ -> },
            onMeaningDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    var buttonEnabled by remember { mutableStateOf(true) }
    MyVocaTheme {
        TopBar(
            screenType = AddWord,
            addButtonEnabled = buttonEnabled,
            onAddWord = { buttonEnabled = !buttonEnabled },
            onClose = {}
        )
    }
}