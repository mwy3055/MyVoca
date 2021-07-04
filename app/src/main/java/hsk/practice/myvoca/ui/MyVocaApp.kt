package hsk.practice.myvoca.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.MyVocaBottomAppBar
import hsk.practice.myvoca.ui.components.MyVocaTopAppBar
import hsk.practice.myvoca.ui.screens.allword.AllWordScreen
import hsk.practice.myvoca.ui.screens.home.HomeScreen
import hsk.practice.myvoca.ui.screens.profile.ProfileScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizScreen
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@ExperimentalAnimationApi
@Composable
fun MyVocaApp() {
    MyVocaTheme {
        val allScreens = MyVocaScreen.values().toList()
        val navController = rememberNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = MyVocaScreen.fromRoute(
            backstackEntry.value?.destination?.route
        )

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
                        if (screen != currentScreen) {
                            navController.navigate(screen.name) {
                                if (screen == MyVocaScreen.Home) {
                                    popUpTo(MyVocaScreen.Home.name)
                                }
                            }
                        }
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            MyVocaNavGraph(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )
        }
    }
}

@Composable
fun MyVocaNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = MyVocaScreen.Home.name,
        modifier = modifier
    ) {
        // TODO: 여기에서 ViewModel 만들어서 넘기기: by viewModel()
        composable(MyVocaScreen.Home.name) {
            HomeScreen()
        }
        composable(MyVocaScreen.AllWord.name) {
            AllWordScreen(hiltViewModel())
        }
        composable(MyVocaScreen.Quiz.name) {
            QuizScreen()
        }
        composable(MyVocaScreen.Profile.name) {
            ProfileScreen()
        }
    }
}