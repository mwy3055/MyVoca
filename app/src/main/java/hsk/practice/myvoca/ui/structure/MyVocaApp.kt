package hsk.practice.myvoca.ui.structure

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.MyVocaBottomAppBar
import hsk.practice.myvoca.ui.components.MyVocaTopAppBar
import hsk.practice.myvoca.ui.screens.allword.AllWordScreen
import hsk.practice.myvoca.ui.screens.allword.AllWordViewModel
import hsk.practice.myvoca.ui.screens.home.HomeScreen
import hsk.practice.myvoca.ui.screens.home.HomeViewModel
import hsk.practice.myvoca.ui.screens.profile.ProfileScreen
import hsk.practice.myvoca.ui.screens.profile.ProfileViewModel
import hsk.practice.myvoca.ui.screens.quiz.QuizScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizViewModel
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MyVocaApp(onLaunch: suspend () -> Unit = {}) {
    MyVocaTheme {
        val systemUiController = rememberSystemUiController()
        val systemBarColor = MaterialTheme.colors.primarySurface
        LaunchedEffect(key1 = true) {
            onLaunch()
            systemUiController.setStatusBarColor(
                color = systemBarColor
            )
        }

        val allScreens = MyVocaScreen.values().toList().toImmutableList()
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentScreen = MyVocaScreen.fromRoute(backStackEntry?.destination?.route)
        Scaffold(
            topBar = {
                MyVocaTopAppBar(currentScreen = currentScreen)
            },
            bottomBar = {
                MyVocaBottomAppBar(
                    allScreens = allScreens,
                    onTabSelected = { screen ->
                        if (screen != currentScreen) {
                            navigateTo(navController, screen)
                        }
                    },
                    currentScreen = currentScreen
                )
            },
        ) { innerPadding ->
            MyVocaNavGraph(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )
        }
    }
}

private fun navigateTo(
    navController: NavHostController,
    screen: MyVocaScreen
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
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    allWordViewModel: AllWordViewModel = hiltViewModel(),
    quizViewModel: QuizViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    // NavHost 밖에서 만들면 화면 전환에 상관없이 유지된다.
    NavHost(
        navController = navController,
        startDestination = MyVocaScreen.Home.name,
        modifier = modifier
    ) {
        // NavHost 안에서 만들면 매번 새로운 객체가 생성된다.
        composable(MyVocaScreen.Home.name) {
            HomeScreen(homeViewModel)
        }
        composable(MyVocaScreen.AllWord.name) {
            AllWordScreen(allWordViewModel)
        }
        composable(MyVocaScreen.Quiz.name) {
            QuizScreen(quizViewModel)
        }
        composable(MyVocaScreen.Profile.name) {
            ProfileScreen(profileViewModel)
        }
    }
}