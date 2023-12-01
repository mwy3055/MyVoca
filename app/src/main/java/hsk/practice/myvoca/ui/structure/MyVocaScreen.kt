package hsk.practice.myvoca.ui.structure

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

enum class MyVocaScreen(
    val icon: ImageVector,
) {
    Home(
        icon = Icons.Outlined.Home
    ),
    AllWord(
        icon = Icons.AutoMirrored.Filled.List,
    ),
    Quiz(
        icon = Icons.Outlined.Quiz,
    ),
    Profile(
        icon = Icons.Filled.AccountCircle
    );

    companion object {
        fun fromRoute(route: String?): MyVocaScreen {
            return route?.let {
                entries.firstOrNull { screen -> route.substringBefore("/") == screen.name }
            } ?: Home
        }
    }
}