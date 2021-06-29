package hsk.practice.myvoca.ui.screens.allword

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun AllWordScreen() {
    AllWordContent()
}

@Composable
fun AllWordContent() {
    Text(
        text = "This is AllWord content",
        style = MaterialTheme.typography.body1
    )
}