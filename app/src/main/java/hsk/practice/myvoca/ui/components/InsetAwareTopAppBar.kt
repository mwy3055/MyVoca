@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding

/**
 * From Jetnews, Google's official sample app.
 *
 * A wrapper around [TopAppBar] which uses [Modifier.statusBarsPadding] to shift the app bar's
 * contents down, but still draws the background behind the status bar too.
 */
@Composable
fun InsetAwareTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 4.dp
) {
    Surface(
        color = backgroundColor,
        shadowElevation = elevation,
        modifier = modifier
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = contentColor,
                titleContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(bottom = false),
        )
    }
}