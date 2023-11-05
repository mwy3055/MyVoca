package hsk.practice.myvoca.ui.structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.coroutines.delay

@Composable
fun Splash(
    loadApp: suspend () -> Unit = {},
    onLaunch: suspend () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SplashNavGraph(
            loadApp = loadApp,
            navController = rememberNavController(),
            onLaunch = onLaunch
        )
    }
}

private const val SplashName = "splash_screen"
private const val MyVocaAppName = "main_app"

@Composable
private fun SplashNavGraph(
    loadApp: suspend () -> Unit,
    navController: NavHostController,
    onLaunch: suspend () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SplashName
    ) {
        composable(SplashName) {
            SplashScreen(
                loadApp = loadApp,
                navController = navController
            )
        }
        composable(MyVocaAppName) {
            MyVocaApp(onLaunch = onLaunch)
        }
    }
}

@Composable
private fun SplashScreen(
    loadApp: suspend () -> Unit,
    navController: NavHostController
) {
    LaunchedEffect(true) {
        loadApp()
        navController.popBackStack()
        navController.navigate(MyVocaAppName)
    }

    Background()
    Logo()
}

@Composable
private fun Background() {
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawLine(
            color = lineColor,
            start = Offset(x = canvasWidth * 2 / 3, y = -100f),
            end = Offset(x = -250f, y = canvasHeight * 2 / 3),
            strokeWidth = 350f
        )
        drawLine(
            color = lineColor,
            start = Offset(x = -250f, y = canvasHeight * 2 / 3),
            end = Offset(x = canvasWidth * 4 / 3, y = canvasHeight * 8 / 9),
            strokeWidth = 350f
        )
    }
}

@Composable
private fun Logo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndex = 1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyVocaText(
            text = "나만의 단어장",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        MyVocaText(
            text = "MyVoca",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MyVocaTheme {
        SplashScreen(
            loadApp = { delay(1000L) },
            navController = rememberNavController()
        )
    }
}