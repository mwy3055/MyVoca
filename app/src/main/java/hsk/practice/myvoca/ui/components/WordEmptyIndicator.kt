package hsk.practice.myvoca.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.R
import hsk.practice.myvoca.ui.screens.addword.AddWordActivity

@Composable
fun WordEmptyIndicator(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyVocaText(
                text = stringResource(R.string.please_register_the_word_first),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddWordButton(
                onClick = {
                    context.startActivity(
                        Intent(context, AddWordActivity::class.java)
                    )
                },
            )
        }
    }
}