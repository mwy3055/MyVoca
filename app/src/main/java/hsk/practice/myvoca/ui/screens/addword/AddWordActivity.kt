package hsk.practice.myvoca.ui.screens.addword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@AndroidEntryPoint
class AddWordActivity : ComponentActivity() {

    // TODO: ViewModel?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyVocaTheme {
                AddWordScreen()
            }
        }
    }
}
