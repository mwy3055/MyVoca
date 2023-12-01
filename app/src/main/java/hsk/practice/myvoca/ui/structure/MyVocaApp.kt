package hsk.practice.myvoca.ui.structure

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.R
import hsk.practice.myvoca.ui.components.MyVocaBottomAppBar
import hsk.practice.myvoca.ui.components.MyVocaTopAppBar
import hsk.practice.myvoca.ui.screens.allword.AllWordScreen
import hsk.practice.myvoca.ui.screens.home.HomeScreen
import hsk.practice.myvoca.ui.screens.profile.ProfileScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MyVocaApp(onLaunch: suspend () -> Unit = {}) {
    MyVocaTheme {
        val systemUiController = rememberSystemUiController()
        val systemBarColor = MaterialTheme.colorScheme.surface
        val context = LocalContext.current
        val allScreens = MyVocaScreen.values().toList().toImmutableList()
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentScreen = MyVocaScreen.fromRoute(backStackEntry?.destination?.route)

        LaunchedEffect(key1 = true) {
            onLaunch()
            systemUiController.setStatusBarColor(
                color = systemBarColor,
            )
        }

        val resultLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val word = result.data?.getStringExtra("word") ?: ""
                    val actionType = result.data?.getStringExtra("actionType") ?: ""
                    if (word.isNotEmpty()) {
                        when(actionType) {
                            "add" -> {
                                Toast.makeText(
                                    context, context.getString(R.string.add_word_complete, word),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            "update" -> {
                                Toast.makeText(
                                    context, context.getString(R.string.update_word_complete, word),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

        Scaffold(
            topBar = {
                MyVocaTopAppBar(
                    currentScreen = currentScreen,
                    resultLauncher = resultLauncher,
                )
            },
            bottomBar = {
                MyVocaBottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    allScreens = allScreens,
                    onTabSelected = { screen ->
                        if (screen != currentScreen) {
                            navigateTo(navController, screen)
                        }
                    },
                    currentScreen = currentScreen,
                )
            }
        ) { innerPadding ->
            MyVocaNavGraph(
                resultLauncher = resultLauncher,
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )
        }
    }
}

private fun navigateTo(
    navController: NavHostController,
    screen: MyVocaScreen,
) {
    navController.navigate(screen.name) {
        if (screen == MyVocaScreen.Home) {
            clearPopUpStack()
        }
    }
}

private fun NavOptionsBuilder.clearPopUpStack() {
    popUpTo(MyVocaScreen.Home.name)
}

@Composable
fun MyVocaNavGraph(
    resultLauncher: ActivityResultLauncher<Intent>,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // NavHost 밖에서 만들면 화면 전환에 상관없이 유지된다.
    NavHost(
        navController = navController,
        startDestination = MyVocaScreen.Home.name,
        modifier = modifier,
    ) {
        // NavHost 안에서 만들면 매번 새로운 객체가 생성된다.
        composable(MyVocaScreen.Home.name) {
            HomeScreen()
        }
        composable(MyVocaScreen.AllWord.name) {
            AllWordScreen(resultLauncher = resultLauncher)
        }
        composable(MyVocaScreen.Quiz.name) {
            QuizScreen()
        }
        composable(MyVocaScreen.Profile.name) {
            ProfileScreen()
        }
    }
}