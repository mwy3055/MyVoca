@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.HourglassFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.web.WebView
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.SystemBarColor
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AddWordScreen(
    updateWordId: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddWordViewModel = hiltViewModel(),
) {
    SystemBarColor(systemBarColor = MaterialTheme.colorScheme.surface)
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
                onClose = onClose,
                elevation = 16.dp
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Content(
                data = uiState,
                loadStatus = viewModel::loadStatus,
                onMeaningAdd = viewModel::onMeaningAdd,
                onWordUpdate = viewModel::onWordUpdate,
                onWordClear = viewModel::onWordClear,
                onMeaningCheck = viewModel::onMeaningCheck,
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
    onClose: () -> Unit,
    elevation: Dp,
) {
    val textAlpha by animateFloatAsState(targetValue = if (addButtonEnabled) 1f else 0.6f)

    InsetAwareTopAppBar(
        title = {
            TopBarTitle(screenType)
        },
        navigationIcon = {
            TopBarCloseButton(onClose = onClose)
        },
        actions = {
            if (showWebView) {
                TopBarCompleteButton(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onHideWebView = onHideWebView
                )
            } else {
                TopBarSaveButton(
                    onAddWord = onAddWord,
                    onClose = onClose,
                    addButtonEnabled = addButtonEnabled,
                    textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                )
            }
        },
        elevation = elevation
    )
}

@Composable
private fun TopBarTitle(screenType: ScreenType) {
    val title = when (screenType) {
        AddWord -> stringResource(R.string.add_word)
        UpdateWord -> stringResource(R.string.update_word)
    }
    MyVocaText(
        text = title,
        color = MaterialTheme.colorScheme.onSurface
    )
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
    onWordClear: () -> Unit,
    onMeaningCheck: (Int, MeaningImpl) -> Unit,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    onMemoUpdate: (String) -> Unit,
    onShowWebView: () -> Unit,
    onUpdateWebViewUrl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight(if (data.showWebView) 0.5f else 1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp)
    ) {
        EssentialTitle()
        Word(
            word = data.word,
            status = data.wordExistStatus,
            loadStatus = loadStatus,
            onWordUpdate = onWordUpdate,
            onWordClear = onWordClear,
            onShowWebView = onShowWebView,
            onUpdateWebViewUrl = onUpdateWebViewUrl
        )
        Meanings(
            meanings = data.meanings,
            statusList = data.meaningExistStatuses,
            onMeaningCheck = onMeaningCheck,
            onMeaningAdd = onMeaningAdd,
            onMeaningUpdate = onMeaningUpdate,
            onMeaningDelete = onMeaningDelete
        )
        Spacer(modifier = Modifier.height(10.dp))
        OptionalTitle()
        Memo(
            memo = data.memo,
            onMemoUpdate = onMemoUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
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
    onWordClear: () -> Unit,
    onShowWebView: () -> Unit,
    onUpdateWebViewUrl: () -> Unit,
    modifier: Modifier = Modifier,
    focusManager: FocusManager = LocalFocusManager.current
) {
    LaunchedEffect(word) {
        loadStatus(word)
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
    )
    val statusIcon = getWordStatusIcon(status)
    val iconColor = getWordStatusIconColor(status)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = word,
                        label = { MyVocaText(text = stringResource(R.string.input_title)) },
                        onValueChange = onWordUpdate,
                        colors = textFieldColors,
                        trailingIcon = {
                            statusIcon?.let {
                                IconButton(
                                    onClick = {
                                        if (statusIcon == Icons.Outlined.Cancel)
                                            onWordClear()
                                    }
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = stringResource(R.string.status_icon_description),
                                        tint = iconColor
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        isError = (status == WordExistStatus.DUPLICATE),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onShowWebView()
                            onUpdateWebViewUrl()
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_for_word),
                        )
                    }
                }
                val duplicateTextColor by animateColorAsState(
                    targetValue = if (status == WordExistStatus.DUPLICATE) iconColor else Color.Transparent,
                    animationSpec = tween(
                        durationMillis = 300,
                    )
                )
                MyVocaText(
                    text = stringResource(R.string.this_word_has_already_been_registerd),
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = duplicateTextColor
                )
            }
        }
    }
}

@Composable
private fun Meanings(
    meanings: ImmutableList<MeaningImpl>,
    statusList: ImmutableList<MeaningExistStatus>,
    onMeaningCheck: (Int, MeaningImpl) -> Unit,
    onMeaningAdd: (WordClassImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        MeaningChips(
            onMeaningAdd = onMeaningAdd
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (meanings.isEmpty()) {
            MeaningsEmptyIndicator(
                modifier = Modifier
                    .height(meaningHeight)
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            )
        } else {
            MeaningsContent(
                meanings = meanings,
                statusList = statusList,
                onMeaningCheck = onMeaningCheck,
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
    LazyRow {
        items(
            items = WordClassImpl.actualValues(),
            key = { it.korean },
        ) {
            WordClassChip(
                wordClass = it,
                onMeaningAdd = onMeaningAdd
            )
        }
    }
}

@Composable
private fun WordClassChip(
    wordClass: WordClassImpl,
    onMeaningAdd: (WordClassImpl) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMeaningAdd(wordClass) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_icon_description)
                )
                WordClassChipText(wordClass = wordClass)
            }
        }
    }
}

@Composable
private fun WordClassChipIcon(
    onMeaningAdd: (WordClassImpl) -> Unit,
    wordClass: WordClassImpl,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { onMeaningAdd(wordClass) },
        modifier = modifier
            .size(30.dp)
            .background(
                color = Color.Transparent,
                shape = CircleShape
            ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.add_icon_description, wordClass)
        )
    }
}

@Composable
private fun WordClassChipText(
    wordClass: WordClassImpl,
    modifier: Modifier = Modifier,
) {
    MyVocaText(
        text = wordClass.korean,
        modifier = modifier.padding(end = 4.dp)
    )
}

@Composable
private fun MeaningsEmptyIndicator(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyVocaText(
                text = stringResource(R.string.please_add_a_meaning),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MeaningsContent(
    meanings: ImmutableList<MeaningImpl>,
    statusList: ImmutableList<MeaningExistStatus>,
    onMeaningCheck: (Int, MeaningImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(meaningHeight)
            .verticalScroll(scrollState)
    ) {
        meanings.forEachIndexed { index, meaning ->
            Meaning(
                index = index,
                meaning = meaning,
                statusList = statusList,
                onMeaningCheck = onMeaningCheck,
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
    statusList: ImmutableList<MeaningExistStatus>,
    onMeaningCheck: (Int, MeaningImpl) -> Unit,
    onMeaningUpdate: (Int, MeaningImpl) -> Unit,
    onMeaningDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(meaning) {
        onMeaningCheck(index, meaning)
    }

    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )
    val statusIcon = getMeaningStatusIcon(statusList[index])
    val iconColor = getMeaningStatusIconColor(statusList[index])

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    value = meaning.content,
                    label = {
                        MyVocaText(
                            text = stringResource(
                                R.string.index_meaning_type_korean,
                                index + 1,
                                meaning.type.korean
                            )
                        )
                    },
                    trailingIcon = {
                        statusIcon?.let {
                            IconButton(
                                onClick = {
                                    if (statusIcon == Icons.Outlined.Cancel)
                                        onMeaningUpdate(index, meaning.copy(content = ""))
                                }
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = stringResource(R.string.status_icon_description),
                                    tint = iconColor
                                )
                            }
                        }
                    },
                    colors = textFieldColors,
                    onValueChange = {
                        onMeaningUpdate(index, meaning.copy(content = it))
                    },
                    singleLine = true,
                    isError = (statusList[index] == MeaningExistStatus.DUPLICATE),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }
                    )
                )
                IconButton(
                    onClick = { onMeaningDelete(index) },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.baseline_delete_outline_24),
                        contentDescription = stringResource(
                            R.string.delete_the_meaning,
                            meaning.content
                        )
                    )
                }
            }
            val duplicateTextColor by animateColorAsState(
                targetValue = if (statusList[index] == MeaningExistStatus.DUPLICATE) iconColor else Color.Transparent,
                animationSpec = tween(
                    durationMillis = 300,
                )
            )
            MyVocaText(
                text = stringResource(R.string.this_meaning_has_already_been_registerd),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = duplicateTextColor
            )
        }

    }
}

@Composable
private fun Memo(
    memo: String,
    onMemoUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )

    OutlinedTextField(
        modifier = modifier,
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
        WordExistStatus.NOT_EXISTS -> Icons.Outlined.Cancel
        WordExistStatus.DUPLICATE -> Icons.Outlined.Error
        WordExistStatus.LOADING -> Icons.Outlined.HourglassFull
        WordExistStatus.WORD_EMPTY -> null
    }
}

@Composable
private fun getWordStatusIconColor(status: WordExistStatus): Color {
    return when (status) {
        WordExistStatus.NOT_EXISTS -> MaterialTheme.colorScheme.onSurfaceVariant
        WordExistStatus.DUPLICATE -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }
}

@Composable
private fun getMeaningStatusIcon(status: MeaningExistStatus): ImageVector? {
    return when (status) {
        MeaningExistStatus.NOT_EXISTS -> Icons.Outlined.Cancel
        MeaningExistStatus.DUPLICATE -> Icons.Outlined.Error
        MeaningExistStatus.LOADING -> Icons.Outlined.HourglassFull
        MeaningExistStatus.MEANING_EMPTY -> null
    }
}

@Composable
private fun getMeaningStatusIconColor(status: MeaningExistStatus): Color {
    return when (status) {
        MeaningExistStatus.NOT_EXISTS -> MaterialTheme.colorScheme.onSurfaceVariant
        MeaningExistStatus.DUPLICATE -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
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
                onWordClear = {},
                onMeaningCheck = { _, _ -> },
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
            statusList = persistentListOf(),
            onMeaningCheck = { _, _ -> },
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
            onClose = {},
            elevation = 16.dp
        )
    }
}