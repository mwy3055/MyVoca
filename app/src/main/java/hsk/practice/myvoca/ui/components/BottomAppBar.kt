package hsk.practice.myvoca.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.ui.MyVocaScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import java.util.*

private val TabHeight = 56.dp
private const val InactiveTabOpacity = 0.60f

private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100

@ExperimentalAnimationApi
@Composable
fun MyVocaBottomAppBar(
    allScreens: List<MyVocaScreen>,
    onTabSelected: (MyVocaScreen) -> Unit,
    currentScreen: MyVocaScreen
) {
    Surface(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            allScreens.forEach { screen ->
                MyVocaTab(
                    modifier = Modifier.weight(1f),
                    text = screen.name.uppercase(Locale.getDefault()),
                    icon = screen.icon,
                    onSelected = { onTabSelected(screen) },
                    selected = (currentScreen == screen)
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun MyVocaTab(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onSelected: () -> Unit,
    selected: Boolean
) {
    val color =
        if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = InactiveTabOpacity)
    val durationMillis = if (selected) TabFadeInAnimationDuration else TabFadeOutAnimationDuration
    val animationSpec = remember {
        tween<Color>(
            durationMillis = durationMillis,
            easing = LinearEasing,
            delayMillis = TabFadeInAnimationDelay
        )
    }

    val tabTintColor by animateColorAsState(
        targetValue = color,
        animationSpec = animationSpec
    )
    Box(
        modifier = modifier
            .height(TabHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = true,
                    radius = Dp.Unspecified,
                    color = Color.Unspecified
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tabTintColor,
        )
    }
}


@ExperimentalAnimationApi
@Preview
@Composable
fun MyVocaBottomAppBarPreview() {
    MyVocaTheme {
        MyVocaBottomAppBar(
            allScreens = MyVocaScreen.values().toList(),
            onTabSelected = { /*TODO*/ },
            currentScreen = MyVocaScreen.Home
        )
    }
}