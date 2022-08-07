package hsk.practice.myvoca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.6f),
    circleAlign: Alignment = Alignment.Center,
    circleFraction: Float = 0.35f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .zIndex(1f)
            .clickable(false) {} // to prevent click event from behind elements
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(circleAlign)
                .fillMaxSize(fraction = circleFraction)
        )
    }
}