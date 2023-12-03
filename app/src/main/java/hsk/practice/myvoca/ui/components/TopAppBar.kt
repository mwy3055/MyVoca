@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.components

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.R
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.structure.MyVocaScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun MyVocaTopAppBar(
    currentScreen: MyVocaScreen,
    modifier: Modifier = Modifier,
    resultLauncher: ActivityResultLauncher<Intent>? = null,
) {
    TopAppBar(
        title = {
            MyVocaTopTitle(currentScreen)
        },
        actions = {
            MyVocaTopActions(
                currentScreen = currentScreen,
                resultLauncher = resultLauncher!!,
            )
        },
        modifier = modifier.shadow(elevation = 12.dp)
    )
}

@Composable
private fun MyVocaTopTitle(
    currentScreen: MyVocaScreen,
    modifier: Modifier = Modifier,
) {
    MyVocaText(
        text = if (currentScreen == MyVocaScreen.Quiz) stringResource(R.string.quiz_screen_title)
        else stringResource(R.string.app_name),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun MyVocaTopNavigationIcon(
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = {},
        modifier = modifier,
        enabled = false,
    ) {
        Icon(
            imageVector = Icons.Filled.ContentPaste,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MyVocaTopActions(
    currentScreen: MyVocaScreen,
    resultLauncher: ActivityResultLauncher<Intent>,
    modifier: Modifier = Modifier,
) {
    if (currentScreen in listOf(MyVocaScreen.Home, MyVocaScreen.AllWord)) {
        val context = LocalContext.current

        IconButton(
            onClick = {
                resultLauncher.launch(getAddWordActivityIntent(context))
            },
            modifier = modifier,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.you_can_add_new_word),
                tint = contentColorFor(MaterialTheme.colorScheme.surface),
            )
        }
    }
}

private fun getAddWordActivityIntent(context: Context) =
    Intent(context, AddWordActivity::class.java)

@Preview
@Composable
private fun MyVocaTopAppBarPreview() {
    MyVocaTheme {
        MyVocaTopAppBar(currentScreen = MyVocaScreen.Home)
    }
}