package hsk.practice.myvoca.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.ui.graphics.vector.ImageVector

enum class MyVocaScreen(
    val icon: ImageVector
) {
    Home(
        icon = Icons.Filled.Home
    ),
    AllWord(
        icon = Icons.Filled.List,
    ),
    Quiz(
        icon = Icons.Filled.QuestionAnswer,
    ),
    Profile(
        icon = Icons.Filled.AccountCircle
    );

    companion object {
        fun fromRoute(route: String?): MyVocaScreen {
            return route?.let {
                values().firstOrNull { screen -> route.substringBefore("/") == screen.name }
            } ?: Home
        }
    }
}