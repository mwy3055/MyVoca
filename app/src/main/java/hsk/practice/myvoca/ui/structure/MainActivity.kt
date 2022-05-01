package hsk.practice.myvoca.ui.structure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyVocaTheme {
                Splash(onLaunch = {
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                })
            }
        }
    }
}