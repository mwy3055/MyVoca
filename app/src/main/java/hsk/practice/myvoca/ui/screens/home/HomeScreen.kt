package hsk.practice.myvoca.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hsk.practice.myvoca.ui.components.LoadingIndicator

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val homeScreenData by viewModel.homeScreenData.collectAsState()

    HomeLoading(homeScreenData)
}

@Composable
fun HomeLoading(data: HomeScreenData) {
    Box {
        if (data.loading) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
            )
        }
        HomeContent(
            data.totalWordCount
        )
    }
}

@Composable
fun HomeContent(size: Int) {
    HomeTitle(size = size)
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