package hsk.practice.myvoca.ui

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.MyVocaBottomAppBar
import hsk.practice.myvoca.ui.components.MyVocaTopAppBar
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity
import hsk.practice.myvoca.ui.screens.allword.AllWordScreen
import hsk.practice.myvoca.ui.screens.allword.AllWordViewModel
import hsk.practice.myvoca.ui.screens.home.HomeScreen
import hsk.practice.myvoca.ui.screens.home.HomeViewModel
import hsk.practice.myvoca.ui.screens.profile.ProfileScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizViewModel
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

        val context = LocalContext.current

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
            },
            floatingActionButton = {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, AddWordActivity::class.java))
                    },
                    modifier = Modifier.background(
                        color = MaterialTheme.colors.secondary,
                        shape = CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "새로운 단어를 추가할 수 있습니다.",
                    )
                }
            },
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
    // 여기서 만들면 화면 전환에 상관없이 유지된다.
    val homeViewModel: HomeViewModel = hiltViewModel()
    val allWordViewModel: AllWordViewModel = hiltViewModel()
    val quizViewModel: QuizViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = MyVocaScreen.Home.name,
        modifier = modifier
    ) {
        // 여기서 만들면 화면을 전환할 때마다 매번 새로운 객체가 생성된다.
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
            ProfileScreen()
        }
    }
}