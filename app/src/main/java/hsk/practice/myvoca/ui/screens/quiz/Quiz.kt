package hsk.practice.myvoca.ui.screens.quiz

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.fakeData
import hsk.practice.myvoca.ui.components.LoadingIndicator
import hsk.practice.myvoca.ui.components.WordContent
import hsk.practice.myvoca.ui.components.WordMeanings
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import hsk.practice.myvoca.util.randoms
import hsk.practice.myvoca.util.truncate

@Composable
fun QuizScreen(viewModel: QuizViewModel) {
    val quizScreenData by viewModel.quizScreenData.collectAsState()

    QuizLoading(
        quizScreenData = quizScreenData,
        onOptionClick = viewModel::onQuizOptionSelected,
        onCloseDialog = viewModel::onResultDialogClose
    )
}

@Composable
fun QuizLoading(
    quizScreenData: QuizScreenData,
    onOptionClick: (Int) -> Unit,
    onCloseDialog: (QuizResultData) -> Unit
) {
    Box(modifier = Modifier.background(MaterialTheme.colors.surface)) {
        when (quizScreenData.quizState) {
            is QuizLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
            is QuizAvailable -> {
                QuizScreen(
                    quizData = quizScreenData.quizData,
                    onOptionClick = onOptionClick
                )
            }
            is QuizNotAvailable -> {
                QuizNotAvailable(need = quizScreenData.numberVocabularyNeed)
            }
        }
        quizScreenData.quizResult?.let { result ->
            QuizResult(
                resultData = result,
                onCloseDialog = onCloseDialog
            )
        }
    }
}

@Composable
fun QuizNotAvailable(need: Int) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
//            modifier = Modifier.align(Alignment.Center),
            text = "${need}개의 단어가 더 필요합니다.",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = {
            context.startActivity(
                Intent(context, AddWordActivity::class.java)
            )
        }) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "클릭하여 단어를 추가합니다."
            )
            Text(text = "단어 추가하러 가기")
        }
    }
}

@Composable
fun QuizScreen(
    quizData: QuizData,
    onOptionClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.weight(2f))
        QuizTitle(quizData.quiz.answer)
        Spacer(modifier = Modifier.weight(1f))
        QuizOptions(
            modifier = Modifier.weight(8f),
            options = quizData.quiz.quizList,
            onOptionClick = onOptionClick
        )
        VersusView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            leftValue = quizData.quizStat.correct,
            rightValue = quizData.quizStat.wrong
        )
    }
}

@Composable
fun QuizTitle(answer: VocabularyImpl) {
    // Reduce text size when overflow
    val textStyleTitle3 = MaterialTheme.typography.h3
    val (textStyle, updateTextStyle) = remember { mutableStateOf(textStyleTitle3) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QuizOptions(
    modifier: Modifier = Modifier,
    options: List<VocabularyImpl>,
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizOption(
    modifier: Modifier = Modifier,
    index: Int,
    option: VocabularyImpl,
    onOptionClick: (Int) -> Unit
) {
    val onClick = { onOptionClick(index) }
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.h5) {
            WordMeanings(
                meanings = option.meaning.truncate(2),
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizResult(
    resultData: QuizResultData,
    onCloseDialog: (QuizResultData) -> Unit
) {
    val title = if (resultData.result is QuizCorrect) "맞았습니다!!" else "틀렸습니다"
    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            WordContent(resultData.answer)
        },
        buttons = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { onCloseDialog(resultData) }) {
                    Text("확인")
                }
            }
        },
        onDismissRequest = { onCloseDialog(resultData) }
    )
}


@Preview(showBackground = true)
@Composable
fun QuizNotAvailablePreview() {
    MyVocaTheme {
        QuizNotAvailable(need = 5)
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    val quizData = QuizData(
        quiz = Quiz(fakeData.randoms(quizSize), answerIndex = (0 until quizSize).random()),
        quizStat = QuizStat(10, 5)
    )
    var text by remember { mutableStateOf("dtd") }

    MyVocaTheme {
        Column {
            Text(text = text)
            QuizScreen(
                quizData = quizData,
                onOptionClick = { text = it.toString() }
            )
        }
    }
}