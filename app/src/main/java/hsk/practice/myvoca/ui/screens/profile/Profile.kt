package hsk.practice.myvoca.ui.screens.profile

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ProfileScreen() {
    ProfileContent()
}

@Composable
fun ProfileContent() {
    Text(
        text = "This is Profile content",
        style = MaterialTheme.typography.body1
    )
}