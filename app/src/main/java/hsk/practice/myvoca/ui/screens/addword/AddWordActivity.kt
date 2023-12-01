package hsk.practice.myvoca.ui.screens.addword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.imePadding
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@AndroidEntryPoint
class AddWordActivity : ComponentActivity() {

    companion object {
        const val updateWordId = "update_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wordId = getUpdateWordId()

        setContent {
            MyVocaTheme {
                val viewModel = hiltViewModel<AddWordViewModel>()
                val uiStateFlow by viewModel.uiStateFlow.collectAsStateWithLifecycle()

                AddWordScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    viewModel = viewModel,
                    updateWordId = wordId,
                    onClose = {
                        val actionType = if (wordId != -1) "update" else "add"
                        setResult(
                            Activity.RESULT_OK,
                            Intent().apply {
                                putExtra("word", uiStateFlow.word)
                                putExtra("actionType", actionType)
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun getUpdateWordId(defaultValue: Int = -1) =
        intent.getIntExtra(updateWordId, defaultValue)
}
