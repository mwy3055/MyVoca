@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.HourglassFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.web.WebView
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.StaggeredGrid
import hsk.practice.myvoca.ui.components.SystemBarColor
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AddWordScreen(
    modifier: Modifier = Modifier,
    viewModel: AddWordViewModel,
    updateWordId: Int,
    onClose: () -> Unit = {}
) {
    SystemBarColor(systemBarColor = MaterialTheme.colorScheme.secondary)
    LaunchedEffect(key1 = true) {
        if (updateWordId != -1) viewModel.injectUpdateTarget(updateWordId)
    }

    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val webViewState = viewModel.webViewState
    val webViewNavigator = viewModel.webViewNavigator
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.webViewUrl) {
        webViewNavigator.loadUrl(
            uiState.webViewUrl.asString(context)
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                screenType = uiState.screenType,
                addButtonEnabled = uiState.canStoreWord,
                onAddWord = viewModel::onAddWord,
                showWebView = uiState.showWebView,
                onHideWebView = viewModel::onHideWebView,
                onClose = onClose
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            Content(
                data = uiState,
                loadStatus = viewModel::loadStatus,
                onMeaningAdd = viewModel::onMeaningAdd,
                onWordUpdate = viewModel::onWordUpdate,
                onMeaningUpdate = viewModel::onMeaningUpdate,
                onMeaningDelete = viewModel::onMeaningDelete,
                onMemoUpdate = viewModel::onMemoUpdate,
                onShowWebView = viewModel::onShowWebView,
                onUpdateWebViewUrl = viewModel::onUpdateWebViewUrl
            )

            if (uiState.showWebView) {
                WebView(
                    state = webViewState,
                    navigator = webViewNavigator,
                    onCreated = { webView ->
                        with(webView) {
                            settings.run {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                javaScriptCanOpenWindowsAutomatically = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    screenType: ScreenType,
    addButtonEnabled: Boolean,
    onAddWord: () -> Unit,
    showWebView: Boolean,
    onHideWebView: () -> Unit,
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
        backgroundColor = MaterialTheme.colorScheme.secondary,
        actions = {
            if (showWebView) {
                TopBarCompleteButton(
                    textColor = Color.White,
                    onHideWebView = onHideWebView
                )
            } else {
                TopBarSaveButton(
                    onAddWord = onAddWord,
                    onClose = onClose,
                    addButtonEnabled = addButtonEnabled,
                    textColor = Color.White.copy(alpha = textAlpha)
                )
            }
        }
    )
}

@Composable
private fun TopBarTitle(screenType: ScreenType) {
    val title = when (screenType) {
        AddWord -> stringResource(R.string.add_word)
        UpdateWord -> stringResource(R.string.update_word)
    }
    MyVocaText(text = title)
}

@Composable
private fun TopBarCloseButton(onClose: () -> Unit) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.close_the_screen)
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
        MyVocaText(
            text = stringResource(R.string.save),
            color = textColor
        )
    }
}

@Composable
private fun TopBarCompleteButton(
    textColor: Color,
    onHideWebView: () -> Unit,
    focusManager: FocusManager = LocalFocusManager.current,
) {
    TextButton(
        onClick = {
            focusManager.clearFocus()
            onHideWebView()
        },
    ) {
        MyVocaText(
            text = stringResource(R.string.complete),
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
    onMemoUpdate: (String) -> Unit,
    onShowWebView: () -> Unit,
    onUpdateWebViewUrl: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(if (data.showWebView) 0.5f else 1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EssentialTitle()
        Word(
            word = data.word,
            status = data.wordExistStatus,
            loadStatus = loadStatus,
            onWordUpdate = onWordUpdate,
            onShowWebView = onShowWebView,
            onUpdateWebViewUrl = onUpdateWebViewUrl
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
    MyVocaText(
        text = stringResource(R.string.required_inputs),
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun OptionalTitle() {
    MyVocaText(
        text = stringResource(R.string.selective_inputs),
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun Word(
    word: String,
    status: WordExistStatus,
    loadStatus: suspend (String) -> Unit,
    onWordUpdate: (String) -> Unit,
    onShowWebView: () -> Unit,
    onUpdateWebViewUrl: () -> Unit,
    focusManager: FocusManager = LocalFocusManager.current
) {
    LaunchedEffect(word) {
        loadStatus(word)
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )
    val statusIcon = getWordStatusIcon(status)
    val iconColor = getWordStatusIconColor(status)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = word,
                    label = { MyVocaText(text = stringResource(R.string.word)) },
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
                MyVocaText(
                    text = stringResource(R.string.this_word_has_already_been_registerd),
                    style = MaterialTheme.typography.bodySmall,
                    color = duplicateTextColor
                )
            }
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    onShowWebView()
                    onUpdateWebViewUrl()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_for_word),
                )
            }
        }
    }
}

@Composable
private fun Meanings(
    meanings: ImmutableList<MeaningImpl>,
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
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
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
            contentDescription = stringResource(R.string.add_meaning, wordClass)
        )
    }
}

@Composable
private fun WordClassChipText(wordClass: WordClassImpl) {
    MyVocaText(
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
            MyVocaText(
                text = stringResource(R.string.please_add_a_meaning),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MeaningsContent(
    meanings: ImmutableList<MeaningImpl>,
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

@Composable
private fun Meaning(
    index: Int,
    meaning: MeaningImpl,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = meaning.content,
            label = {
                MyVocaText(text = stringResource(
                    R.string.index_meaning_type_korean,
                    index + 1,
                    meaning.type.korean
                ))
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
                contentDescription = stringResource(R.string.delete_the_meaning, meaning.content)
            )
        }
    }
}

@Composable
private fun Memo(
    memo: String,
    onMemoUpdate: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = memo,
        label = {
            MyVocaText(text = stringResource(R.string.memo))
        },
        colors = textFieldColors,
        onValueChange = onMemoUpdate,
        trailingIcon = {
            AnimatedVisibility(visible = memo.isNotEmpty()) {
                IconButton(onClick = { onMemoUpdate("") }) {
                    Icon(
                        imageVector = Icons.Outlined.HighlightOff,
                        contentDescription = stringResource(R.string.clear_english_text)
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
        WordExistStatus.NOT_EXISTS -> MaterialTheme.colorScheme.primary
        WordExistStatus.DUPLICATE -> MaterialTheme.colorScheme.error
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
                onMemoUpdate = {},
                onShowWebView = {},
                onUpdateWebViewUrl = {}
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
            showWebView = false,
            onHideWebView = {},
            onClose = {}
        )
    }
}