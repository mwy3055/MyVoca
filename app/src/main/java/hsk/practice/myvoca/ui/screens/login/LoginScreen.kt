package hsk.practice.myvoca.ui.screens.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hsk.practice.myvoca.R
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
        Row {
            Spacer(modifier = Modifier.weight(1f))
            LoginScreenBody(
                modifier = Modifier,
                email = data.email,
                password = data.password,
                showPassword = data.showPassword,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                onLogin = viewModel::onLogin
            )
            Spacer(modifier = Modifier.weight(1f))
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
private fun LoginScreenBody(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    showPassword: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        LoginTitle()
        Spacer(modifier = Modifier.weight(1f))
        LoginBody(
            email = email,
            password = password,
            showPassword = showPassword,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onLogin = onLogin
        )
        Spacer(modifier = Modifier.weight(1f))
        LoginDivider()
        Spacer(modifier = Modifier.weight(1f))
        SignInButtons()
        Spacer(modifier = Modifier.weight(5f))
    }
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
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
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
        // Login button
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
        ) {
            Text(text = "로그인")
        }
    }
}

@Composable
private fun LoginDivider() {
    Divider(modifier = Modifier.fillMaxWidth())
}

private data class SignInButton(
    @DrawableRes val iconId: Int,
    val title: String,
    val backgroundColor: Color
)

/* TODO: SignInButton의 정의에 onClick을 추가하여 ViewModel로 옮기기.
*   버튼 리스트는 StateFlow에 넣지 않고 상수로 선언해도 상관없음 */

@Composable
private fun SignInButtons() {
    val buttons = listOf(
        SignInButton(
            iconId = R.drawable.ic_round_email_24,
            title = "이메일로 가입",
            backgroundColor = MaterialTheme.colors.primary
        ),
        SignInButton(
            iconId = R.drawable.ic_btn_google_light_normal_ios,
            title = "Google로 로그인",
            backgroundColor = Color.White
        ),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        buttons.forEach { button ->
            MySignInButton(
                iconId = button.iconId,
                title = button.title,
                backgroundColor = button.backgroundColor,
            )
        }
    }
}

@Composable
private fun MySignInButton(
    @DrawableRes iconId: Int,
    title: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        backgroundColor = backgroundColor,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = iconId), contentDescription = title)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenBodyPreview() {
    var email by remember { mutableStateOf("test@gmail.com") }
    var password by remember { mutableStateOf("abcd") }
    var showPassword by remember { mutableStateOf(false) }
    MyVocaTheme {
        LoginScreenBody(
            email = email,
            password = password,
            showPassword = showPassword,
            onEmailChange = { newEmail -> email = newEmail },
            onPasswordChange = { newPassword -> password = newPassword },
            onTogglePasswordVisibility = { showPassword = !showPassword },
            onLogin = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignInButtonEmailPreview() {
    MyVocaTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            SignInButtons()
        }
    }
}