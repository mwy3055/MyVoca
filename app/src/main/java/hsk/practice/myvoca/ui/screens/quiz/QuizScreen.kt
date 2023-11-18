@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.screens.quiz

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsk.ktx.distinctRandoms
import com.hsk.ktx.truncate
import hsk.practice.myvoca.R
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.components.WordMeanings
import hsk.practice.myvoca.ui.components.versus.VersusView
import hsk.practice.myvoca.ui.components.versus.rememberVersusViewState
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// TODO 중복으로 선택지가 나오는 경우를 제거 해야 함
@Composable
fun QuizScreen(
    viewModel: QuizViewModel = hiltViewModel()
) {
    val quizScreenData by viewModel.quizScreenData.collectAsStateWithLifecycle()

    Loading(
        quizScreenData = quizScreenData,
        onOptionClick = viewModel::onQuizOptionSelected,
        onCloseDialog = viewModel::onResultDialogClose
    )
}

@Composable
private fun Loading(
    quizScreenData: QuizScreenData,
    onOptionClick: (Int) -> Unit,
    onCloseDialog: (QuizResultData) -> Unit
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        when (quizScreenData.quizState) {
            is QuizAvailable -> {
                QuizContent(
                    quiz = quizScreenData.quiz,
                    quizStat = quizScreenData.quizStat,
                    onOptionClick = onOptionClick
                )
            }

            is QuizNotAvailable -> {
                QuizNotAvailable(need = quizScreenData.numberVocabularyNeed)
            }

            else -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }
        if (quizScreenData.quizResult != null) {
            Result(
                resultData = quizScreenData.quizResult,
                onCloseDialog = onCloseDialog
            )
        }
    }
}

@Composable
private fun QuizNotAvailable(need: Int) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MyVocaText(
            text = stringResource(R.string.need_more_words, need),
            color = Color.Black,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, AddWordActivity::class.java)
                )
            },
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.click_to_add_word)
            )
            MyVocaText(
                text = stringResource(R.string.go_to_add_word),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuizContent(
    quiz: Quiz,
    quizStat: QuizStat,
    onOptionClick: (Int) -> Unit
) {
    val versusViewState =
        rememberVersusViewState(leftValue = quizStat.correct, rightValue = quizStat.wrong)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        QuizTitle(quiz.answer)
        Spacer(modifier = Modifier.weight(1f))
        QuizOptions(
            modifier = Modifier.weight(8f),
            options = quiz.quizWords,
            onOptionClick = onOptionClick
        )
        VersusView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            versusViewState = versusViewState
        )
    }
}

@Composable
private fun QuizTitle(answer: VocabularyImpl) {
    // Reduce text size when overflow
    val textStyleTitle3 = MaterialTheme.typography.displaySmall
    val (textStyle, updateTextStyle) = remember { mutableStateOf(textStyleTitle3) }
    Box(modifier = Modifier.fillMaxWidth()) {
        MyVocaText(
            text = answer.eng,
            style = textStyle,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    updateTextStyle(textStyle.copy(fontSize = textStyle.fontSize * 0.9))
                }
            }
        )
    }
}

@Composable
private fun QuizOptions(
    modifier: Modifier = Modifier,
    options: ImmutableList<VocabularyImpl>,
    onOptionClick: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        options.forEachIndexed { index, option ->
            QuizOption(
                index = index,
                option = option,
                onOptionClick = onOptionClick
            )
        }
    }
}

@Composable
private fun QuizOption(
    modifier: Modifier = Modifier,
    index: Int,
    option: VocabularyImpl,
    onOptionClick: (Int) -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = { onOptionClick(index) }),
        shape = RoundedCornerShape(16.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp),

        ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
            Box(
                modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                WordMeanings(meanings = option.meaning.truncate(2).toImmutableList())
            }
        }
    }
}

@Composable
private fun Result(
    resultData: QuizResultData,
    onCloseDialog: (QuizResultData) -> Unit
) {
    val title = if (resultData.result is QuizCorrect) stringResource(R.string.correct)
    else stringResource(R.string.incorrect)

    AlertDialog(
        title = {
            MyVocaText(text = title)
        },
        text = {
            WordContent(resultData.answer)
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = { onCloseDialog(resultData) }
                ) {
                    MyVocaText(
                        text = stringResource(id = R.string.check),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }
            }
        },
        onDismissRequest = { onCloseDialog(resultData) },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
}

@Preview(showBackground = true)
@Composable
private fun QuizNotAvailablePreview() {
    MyVocaTheme {
        QuizNotAvailable(need = 5)
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizScreenPreview() {
    val quiz = Quiz(
        quizWords = fakeData.distinctRandoms(quizSize).toImmutableList(),
        answerIndex = (0 until quizSize).random()
    )
    val quizStat = QuizStat(10, 5)

    var text by remember { mutableStateOf("dtd") }

    MyVocaTheme {
        Column {
            MyVocaText(text = text)
            QuizContent(
                quiz = quiz,
                quizStat = quizStat,
                onOptionClick = { text = it.toString() }
            )
        }
    }
}