package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AddWordScreen() {
    AddWordContent()
}

@Composable
fun AddWordContent() {
    Text(text = "hello!")
}

@Preview
@Composable
fun AddWordScreenPreview() {
    MyVocaTheme {
        AddWordContent()
    }
}