package hsk.practice.myvoca.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import hsk.practice.myvoca.ui.screens.allword.AllWordScreen
import hsk.practice.myvoca.ui.screens.home.HomeScreen
import hsk.practice.myvoca.ui.screens.profile.ProfileScreen
import hsk.practice.myvoca.ui.screens.quiz.QuizScreen

enum class MyVocaScreen(
    val icon: ImageVector,
    val body: @Composable () -> Unit
) {
    Home(
        icon = Icons.Filled.Home,
        body = { HomeScreen() }
    ),
    AllWord(
        icon = Icons.Filled.List,
        body = { AllWordScreen() },
    ),
    Quiz(
        icon = Icons.Filled.QuestionAnswer,
        body = { QuizScreen() },
    ),
    Profile(
        icon = Icons.Filled.AccountCircle,
        body = { ProfileScreen() }
    );
}