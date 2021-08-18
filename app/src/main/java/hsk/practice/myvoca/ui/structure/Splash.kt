package hsk.practice.myvoca.ui.structure

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pages
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
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

    val t = remember { Animatable(initialValue = 1f, visibilityThreshold = 10f) }

    val titleSize by animateFloatAsState(
        targetValue = 3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    LaunchedEffect(true) {
        t.animateTo(
            targetValue = 3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
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
        Icon(
            imageVector = Icons.Outlined.Pages,
            contentDescription = null,
            modifier = Modifier.scale(t.value)
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