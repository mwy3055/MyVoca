package hsk.practice.myvoca.ui.structure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun Splash() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        SplashNavGraph(navController = navController)
    }
}

private val SplashName = "splash_screen"
private val MyVocaAppName = "main_app"

@Composable
private fun SplashNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SplashName
    ) {
        composable(SplashName) {
            SplashScreen(navController = navController)
        }
        composable(MyVocaAppName) {
            MyVocaApp()
        }
    }
}

// 스플래시 화면

@Composable
private fun SplashScreen(navController: NavHostController) {
    // 1.5초 후에 메인 화면으로 이동
    LaunchedEffect(true) {
        delay(1000L)
        navController.popBackStack()
        navController.navigate(MyVocaAppName)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Splash?")
    }
}