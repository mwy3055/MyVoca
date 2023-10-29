package hsk.practice.myvoca.ui.components.versus

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hsk.ktx.gcd
import hsk.practice.myvoca.ui.theme.Paybooc

fun VersusViewState(
    leftValue: Int = 0,
    rightValue: Int = 0
): VersusViewState = VersusViewStateImpl(leftValue, rightValue)

private val VersusViewStateSaver = listSaver(
    save = { listOf(it.leftValue, it.rightValue) },
    restore = { VersusViewState(it[0], it[1]) }
)

@Composable
fun rememberVersusViewState(
    leftValue: Int = 0,
    rightValue: Int = 0
) = rememberSaveable(leftValue, rightValue, saver = VersusViewStateSaver) {
    VersusViewState(
        leftValue,
        rightValue
    )
}

private class VersusViewStateImpl(
    leftValue: Int = 0,
    rightValue: Int = 0
) : VersusViewState {
    private var _leftValue by mutableStateOf(leftValue, structuralEqualityPolicy())

    override var leftValue: Int
        get() = _leftValue
        set(value) {
            _leftValue = value
        }

    private var _rightValue by mutableStateOf(rightValue, structuralEqualityPolicy())
    override var rightValue: Int
        get() = _rightValue
        set(value) {
            _rightValue = value
        }
}

@Composable
fun VersusView(
    modifier: Modifier = Modifier,
    versusViewState: VersusViewState = rememberVersusViewState(),
    leftColor: Color = MaterialTheme.colorScheme.primary,
    rightColor: Color = MaterialTheme.colorScheme.secondary
) {
    val leftValue = versusViewState.leftValue
    val rightValue = versusViewState.rightValue
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
    Box(
        modifier = modifier
            .background(background)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                )
            )
    ) {
        Text(
            text = value.toString(),
            fontFamily = Paybooc,
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
    val versusViewState = rememberVersusViewState(leftValue = 3, rightValue = 4)
    VersusView(
        modifier = Modifier
            .height(50.dp)
            .padding(10.dp),
        versusViewState = versusViewState
    )
}

@Preview
@Composable
fun VersusViewPreview_Zero() {
    val versusViewState = rememberVersusViewState(leftValue = 0, rightValue = 3)
    VersusView(
        modifier = Modifier
            .height(50.dp)
            .padding(10.dp),
        versusViewState = versusViewState
    )
}