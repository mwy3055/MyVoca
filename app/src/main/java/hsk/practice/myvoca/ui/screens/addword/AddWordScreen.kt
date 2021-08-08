package hsk.practice.myvoca.ui.screens.addword

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel,
    onClose: () -> Unit = {}
) {
    val systemBarColor = MaterialTheme.colors.primaryVariant
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = systemBarColor
        )
    }

    val data by viewModel.addWordScreenData.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = { AddWordTopBar(onClose = onClose) }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            AddWordContent(data = data)
        }
    }
}

@Composable
private fun AddWordTopBar(onClose: () -> Unit) {
    InsetAwareTopAppBar(
        title = {
            Text(text = "단어 추가")
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "단어 추가 화면을 닫습니다."
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        actions = {
            TextButton(onClick = {
                // TODO: 단어 저장하기
                // onStoreWord(...)
                onClose()
            }) {
                Text(
                    text = "저장",
                    color = Color.White
                )
            }
        }
    )
}

@Composable
fun AddWordContent(data: AddWordScreenData) {
    Text(text = "hello!")
}

@Preview(showBackground = true)
@Composable
fun AddWordScreenPreview() {
    val data = AddWordScreenData()
    MyVocaTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            AddWordContent(data)
        }
    }
}