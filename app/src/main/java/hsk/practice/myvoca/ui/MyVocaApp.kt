package hsk.practice.myvoca.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.primarySurface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.MyVocaBottomAppBar
import hsk.practice.myvoca.ui.components.MyVocaTopAppBar
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@ExperimentalAnimationApi
@Composable
fun MyVocaApp() {
    MyVocaTheme {
        val allScreens = MyVocaScreen.values().toList()
        var currentScreen by rememberSaveable { mutableStateOf(MyVocaScreen.Home) }

        val systemUiController = rememberSystemUiController()
        val systemBarColor = MaterialTheme.colors.primarySurface
        SideEffect {
            systemUiController.setStatusBarColor(
                color = systemBarColor
            )
        }

        Scaffold(
            topBar = {
                MyVocaTopAppBar()
            },
            bottomBar = {
                MyVocaBottomAppBar(
                    allScreens = allScreens,
                    onTabSelected = { screen ->
                        currentScreen = screen
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                currentScreen.body()
            }
        }
    }
}