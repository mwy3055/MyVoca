@file:OptIn(ExperimentalMaterial3Api::class)

package hsk.practice.myvoca.ui.components

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import hsk.practice.myvoca.R
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.structure.MyVocaScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.coroutines.launch

@Composable
fun MyVocaTopAppBar(currentScreen: MyVocaScreen) {
    TopAppBar(
        title = {
            MyVocaTopTitle()
        },
        navigationIcon = {
            MyVocaTopNavigationIcon()
        },
        actions = {
            MyVocaTopActions(currentScreen = currentScreen)
        },
    )
}

@Composable
private fun MyVocaTopTitle() {
    MyVocaText(
        text = stringResource(R.string.app_name),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
private fun MyVocaTopNavigationIcon() {
    IconButton(enabled = false, onClick = {}) {
        Icon(
            imageVector = Icons.Filled.ContentPaste,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MyVocaTopActions(currentScreen: MyVocaScreen) {
    if (currentScreen in listOf(MyVocaScreen.Home, MyVocaScreen.AllWord)) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        IconButton(
            onClick = {
                coroutineScope.launch {
                    context.startActivity(getAddWordActivityIntent(context))
                }
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.you_can_add_new_word),
                tint = contentColorFor(MaterialTheme.colorScheme.surface)
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