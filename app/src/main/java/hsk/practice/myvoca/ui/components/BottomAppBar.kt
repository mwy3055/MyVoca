package hsk.practice.myvoca.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import hsk.practice.myvoca.ui.structure.MyVocaScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.*

private val TabHeight = 56.dp
private const val InactiveTabOpacity = 0.60f

private const val TabFadeInDuration = 150
private const val TabFadeInDelay = 100
private const val TabFadeOutDuration = 100

@Composable
fun MyVocaBottomAppBar(
    allScreens: ImmutableList<MyVocaScreen>,
    onTabSelected: (MyVocaScreen) -> Unit,
    currentScreen: MyVocaScreen,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            allScreens.forEach { screen ->
                MyVocaTab(
                    modifier = Modifier.weight(1f),
                    text = screen.name.uppercase(Locale.getDefault()),
                    icon = screen.icon,
                    onSelected = { onTabSelected(screen) },
                    selected = (currentScreen == screen),
                )
            }
        }
    }
}

@Composable
private fun MyVocaTab(
    text: String,
    icon: ImageVector,
    onSelected: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val color =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.outline
    val durationMillis = if (selected) TabFadeInDuration else TabFadeOutDuration
    val animationSpec = remember(durationMillis) {
        tween<Color>(
            durationMillis = durationMillis,
            easing = LinearEasing,
            delayMillis = TabFadeInDelay,
        )
    }

    val tabTintColor by animateColorAsState(
        targetValue = color,
        animationSpec = animationSpec,
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
                    color = Color.Unspecified,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(32.dp),
            tint = tabTintColor,
        )
    }
}


@Preview
@Composable
fun MyVocaBottomAppBarPreview() {
    MyVocaTheme {
        MyVocaBottomAppBar(
            allScreens = MyVocaScreen.entries.toImmutableList(),
            onTabSelected = { },
            currentScreen = MyVocaScreen.Home,
        )
    }
}