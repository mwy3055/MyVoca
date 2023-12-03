package hsk.practice.myvoca.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hsk.practice.myvoca.R

@Composable
fun SearchResultEmptyIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        MyVocaText(
            text = stringResource(R.string.no_search_results),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}