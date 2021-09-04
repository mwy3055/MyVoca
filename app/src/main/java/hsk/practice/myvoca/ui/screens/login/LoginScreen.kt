package hsk.practice.myvoca.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.ui.components.InsetAwareTopAppBar
import hsk.practice.myvoca.ui.theme.MyVocaTheme

/** TODO
 *  참고: https://ericampire.com/firebase-auth-with-jetpack-compose
 *  1. 로그인 UI를 만들고
 *  2. Google로 로그인 버튼을 추가
 */

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    // Set system bar color
    val systemBarColor = MaterialTheme.colors.secondary
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = systemBarColor)
    }

    val data by viewModel.loginScreenData.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { LoginTopAppBar(onClose = onClose) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Spacer(modifier = Modifier.weight(1f))
            LoginTitle()
            LoginBody(
                email = data.email,
                password = data.password,
                showPassword = data.showPassword,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility
            )
            LoginDivider()
        }
    }
}

@Composable
private fun LoginTopAppBar(onClose: () -> Unit) {
    InsetAwareTopAppBar(
        title = { Text(text = "로그인") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "로그인 화면을 닫습니다."
                )
            }
        },
        backgroundColor = MaterialTheme.colors.secondary
    )
}

@Composable
private fun LoginTitle() {
    Text(
        text = "MyVoca",
        style = MaterialTheme.typography.h2
    )
}

@Composable
private fun LoginBody(
    email: String,
    password: String,
    showPassword: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("이메일") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("비밀번호") },
            trailingIcon = {
                if (password.isNotEmpty()) {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showPassword) "비밀번호를 숨깁니다." else "비밀번호를 보입니다."
                        )
                    }
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
        )
    }
}

@Composable
private fun LoginDivider() {
    Row {
        Spacer(modifier = Modifier.weight(1f))
        Divider(modifier = Modifier.weight(3f))
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun LoginTopAppBarPreview() {
    MyVocaTheme {
        LoginTopAppBar(onClose = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginBodyPreview() {
    var email by remember { mutableStateOf("test@gmail.com") }
    var password by remember { mutableStateOf("abcd") }
    var showPassword by remember { mutableStateOf(false) }
    MyVocaTheme {
        LoginBody(
            email = email,
            password = password,
            showPassword = showPassword,
            onEmailChange = { newEmail -> email = newEmail },
            onPasswordChange = { newPassword -> password = newPassword },
            onTogglePasswordVisibility = { showPassword = !showPassword }
        )
    }
}