package hsk.practice.myvoca.ui.screens.profile

import android.content.Intent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import hsk.practice.myvoca.ui.screens.login.LoginActivity

@Composable
fun ProfileScreen() {
    ProfileContent()
}

@Composable
fun ProfileContent() {
    val context = LocalContext.current
    Text(
        text = "This is Profile content",
        style = MaterialTheme.typography.body1
    )
    Button(onClick = { context.startActivity(Intent(context, LoginActivity::class.java)) }) {
        Text("Go to Login activity")
    }
}