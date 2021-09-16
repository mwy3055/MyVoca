package hsk.practice.myvoca.ui.screens.profile

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import hsk.practice.myvoca.R
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val data by viewModel.profileScreenData.collectAsState()

    ProfileContent(
        user = data.user,
        onTryLogin = viewModel::onTryLogin,
        onLoginButtonClick = viewModel::onLoginButtonClick,
        onLogout = viewModel::onLogout
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ProfileContent(
    user: UserImpl?,
    onTryLogin: (ActivityResult) -> Unit,
    onLoginButtonClick: (Context, ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit,
    onLogout: () -> Unit
) {
    val profileImageSize = 150
    val profileImage = if (user?.profileImageUrl == null) {
        rememberDrawablePainter(
            drawable = AppCompatResources.getDrawable(
                LocalContext.current,
                R.drawable.ic_outline_person_24
            )
        )
    } else {
        rememberImagePainter(
            data = user.profileImageUrl,
            builder = {
                size(profileImageSize)
                transformations(CircleCropTransformation())
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        ProfileImage(
            modifier = Modifier
                .size(profileImageSize.dp)
                .padding(start = 20.dp),
            painter = profileImage
        )

        if (user != null) {
            ProfileUserDetail(
                username = user.username ?: "알 수 없는 이름입니다.",
                email = user.email ?: "알 수 없는 이메일입니다.",
                onLogout = onLogout
            )
        } else {
            ProfileLogin(
                onTryLogin = onTryLogin,
                onLoginButtonClick = onLoginButtonClick
            )
        }

        ProfileNotFullyImplemented(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        )
        Spacer(modifier = Modifier.weight(3f))
    }
}

@Composable
private fun ProfileImage(
    modifier: Modifier = Modifier,
    painter: Painter
) {
    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = "프로필 이미지",
    )
}

@Composable
private fun ProfileUserDetail(
    username: String, email: String,
    onLogout: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.h5
            )
            Text(
                text = email,
                style = MaterialTheme.typography.body1
            )
        }

        TextButton(
            onClick = onLogout
        ) {
            Text(text = "로그아웃")
        }
    }
}

@Composable
private fun ProfileLogin(
    onTryLogin: (ActivityResult) -> Unit,
    onLoginButtonClick: (Context, ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit
) {
    val googleLoginLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            onTryLogin(result)
        }

    val context = LocalContext.current
    TextButton(onClick = { onLoginButtonClick(context, googleLoginLauncher) }) {
        Text(text = "로그인하기")
    }
}

@Composable
private fun ProfileNotFullyImplemented(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.Gray) {
            Text(text = "아직 아무것도 없습니다.")
            Text(text = "추후 기능이 업데이트될 예정입니다.")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    MyVocaTheme {
        ProfileContent(
            user = UserImpl(uid = "dtd", username = "hsk", email = null, profileImageUrl = null),
            onTryLogin = {},
            onLoginButtonClick = { _, _ -> },
            onLogout = {}
        )
    }
}