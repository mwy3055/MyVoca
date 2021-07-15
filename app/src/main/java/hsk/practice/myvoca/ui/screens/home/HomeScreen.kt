package hsk.practice.myvoca.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen() {
    HomeContent()
}

@Composable
fun HomeContent() {
    HomeTitle()
}

@Composable
fun HomeTitle(size: Int = 0) {
    val titleText = if (size == 0) "등록된 단어가\n없습니다" else "${size}개의 단어가\n등록되어 있어요"
    Text(
        text = titleText,
        maxLines = 2,
        style = MaterialTheme.typography.h3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun HomeTitlePreview() {
    HomeTitle(size = 4)
}