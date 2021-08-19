package hsk.practice.myvoca.ui.screens.addword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.imePadding
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@AndroidEntryPoint
class AddWordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyVocaTheme {
                AddWordScreen(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    viewModel = hiltViewModel(),
                    onClose = { finish() }
                )
            }
        }
    }
}
