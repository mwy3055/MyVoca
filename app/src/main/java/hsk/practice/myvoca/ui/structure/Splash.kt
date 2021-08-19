package hsk.practice.myvoca.ui.structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import hsk.practice.myvoca.ui.theme.MyVocaTheme
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

//    val titleSize by animateFloatAsState(
//        targetValue = 3f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioLowBouncy,
//            stiffness = Spring.StiffnessMedium
//        )
//    )

    SplashBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndex = 1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "나만의 단어장",
            style = MaterialTheme.typography.h4
        )
        Text(
            text = "MyVoca",
            style = MaterialTheme.typography.h4
        )
    }
}

@Composable
private fun SplashBackground() {
    val lineColor = MaterialTheme.colors.primary
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

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MyVocaTheme {
        SplashScreen(navController = rememberNavController())
    }
}