package hsk.practice.myvoca.ui.screens.quiz

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun QuizScreen() {
    QuizContent()
}

@Composable
fun QuizContent() {
    Text(
        text = "This is quiz content",
        style = MaterialTheme.typography.body1
    )
}