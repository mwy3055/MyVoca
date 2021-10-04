package hsk.practice.myvoca.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.util.gcd

@Composable
fun VersusView(
    modifier: Modifier = Modifier,
    leftValue: Int,
    rightValue: Int,
    leftColor: Color = MaterialTheme.colors.primary,
    rightColor: Color = MaterialTheme.colors.primaryVariant
) {
    val gcd = leftValue.gcd(rightValue)
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalContentColor provides leftColor) {
            if (leftValue > 0) {
                VersusElement(
                    modifier = Modifier
                        .weight((leftValue / gcd).toFloat()),
                    align = Alignment.CenterStart,
                    value = leftValue
                )
            }
        }
        CompositionLocalProvider(LocalContentColor provides rightColor) {
            if (rightValue > 0) {
                VersusElement(
                    modifier = Modifier
                        .weight((rightValue / gcd).toFloat()),
                    align = Alignment.CenterEnd,
                    value = rightValue
                )
            }
        }
    }
}

@Composable
fun VersusElement(
    modifier: Modifier = Modifier,
    align: Alignment = Alignment.CenterStart,
    value: Int,
) {
    val background = LocalContentColor.current
    Box(modifier = modifier.background(background)) {
        Text(
            text = value.toString(),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(align),
            color = contentColorFor(backgroundColor = background)
        )
    }
}

@Preview
@Composable
fun VersusViewPreview() {
    VersusView(
        modifier = Modifier
            .height(50.dp)
            .padding(10.dp),
        leftValue = 3,
        rightValue = 4
    )
}

@Preview
@Composable
fun VersusViewPreview_Zero() {
    VersusView(
        modifier = Modifier
            .height(50.dp)
            .padding(10.dp),
        leftValue = 0,
        rightValue = 3
    )
}