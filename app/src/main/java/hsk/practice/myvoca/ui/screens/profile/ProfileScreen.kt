package hsk.practice.myvoca.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val data by viewModel.profileScreenData.collectAsState()

    Content(
        data = data,
        onTryLogin = viewModel::onTryLogin,
        onLoginButtonClick = viewModel::onLoginButtonClick,
        onLogout = viewModel::onLogout,
    )
}

@Composable
private fun Content(
    data: ProfileScreenData,
    onTryLogin: (ActivityResult) -> Unit,
    onLoginButtonClick: (Context, ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit,
    onLogout: () -> Unit,
) {
    val user = data.user
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        ProfileImage(
            modifier = Modifier
                .padding(start = 20.dp),
            imageUrl = user?.profileImageUrl
        )

        Box(
            modifier = Modifier.height(48.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (user == null) {
                Login(
                    onTryLogin = onTryLogin,
                    onLoginButtonClick = onLoginButtonClick
                )
            } else {
                UserInfo(
                    username = user.username ?: "알 수 없는 이름입니다.",
                    email = user.email ?: "알 수 없는 이메일입니다.",
                    onLogout = onLogout
                )
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        UserActions(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            user = user,
            uploadActionData = data.uploadActionData,
            downloadActionData = data.downloadActionData,
        )
    }
}

@Composable
private fun ProfileImage(
    modifier: Modifier = Modifier,
    imageUrl: Uri?
) {
    val imageSize = 150
    val modifierWithSize = modifier.size(imageSize.dp)
    if (imageUrl == null) {
        Icon(
            modifier = modifierWithSize,
            painter = rememberVectorPainter(image = Icons.Outlined.HelpOutline),
            contentDescription = "아직 로그인되지 않았습니다.",
            tint = MaterialTheme.colors.onBackground,
        )
    } else {
        Image(
            modifier = modifierWithSize,
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(imageSize)
                    .transformations(CircleCropTransformation())
                    .build()
            ),
            contentDescription = "프로필 이미지",
        )
    }
}

@Composable
private fun UserInfo(
    username: String,
    email: String,
    onLogout: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Username(username = username)
            UserEmail(email = email)
        }
        LogOutButton(onLogout)
    }
}

@Composable
private fun LogOutButton(onLogout: () -> Unit) {
    TextButton(onClick = onLogout) {
        Text(text = "로그아웃")
    }
}

@Composable
private fun UserEmail(email: String) {
    Text(
        text = email,
        style = MaterialTheme.typography.body1
    )
}

@Composable
private fun Username(username: String) {
    Text(
        text = username,
        style = MaterialTheme.typography.h5
    )
}

@Composable
private fun Login(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserActions(
    modifier: Modifier = Modifier,
    user: UserImpl?,
    uploadActionData: UploadActionData,
    downloadActionData: DownloadActionData,
) {
    val clickable = user != null
    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 100.dp), modifier = modifier) {
        item {
            UserActionUploadWords(
                data = uploadActionData,
                enabled = clickable,
            )
        }
        item {
            UserActionDownloadWords(
                data = downloadActionData,
            )
        }
    }
}

private const val actionIconFraction = 0.4f

@Composable
private fun UserActionUploadWords(
    data: UploadActionData,
    enabled: Boolean,
) {
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
    val color by animateColorAsState(
        targetValue = when {
            data.finished -> Color.Green
            data.uploading -> MaterialTheme.colors.primary
            else -> MaterialTheme.colors.onBackground
        }
    )
    val actionData = data.actionData
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = data.onClick)
            .aspectRatio(1f)
            .alpha(alpha),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = actionData.icon,
                contentDescription = actionData.text,
                modifier = Modifier.fillMaxSize(fraction = actionIconFraction),
                tint = color
            )
            if (data.uploadProgress != null) {
                CircularProgressIndicator(
                    modifier = Modifier.scale(1.75f),
                    progress = data.uploadProgress,
                    color = color,
                    strokeWidth = 3.dp
                )
            }
        }
        Text(text = actionData.text)
    }

    if (data.showUploadDialog) {
        UploadDialog(onConfirm = data.onConfirm, onDismiss = data.onDismiss)
    }
}

@Composable
private fun UserActionDownloadWords(data: DownloadActionData) {
    val enabled = (data.downloadPossible == true)
    val actionData = data.actionData

    val contentAlpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = data.onClick)
            .aspectRatio(1f)
            .alpha(contentAlpha),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            imageVector = actionData.icon,
            contentDescription = actionData.text,
            modifier = Modifier.fillMaxSize(fraction = actionIconFraction)
        )
        Text(text = actionData.text)
    }

    if (data.showDownloadDialog) {
        DownloadDialog(onConfirm = data.onConfirm, onDismiss = data.onDismiss)
    }
}

@Composable
private fun UploadDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = "단어 백업하기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "단어를 백업하시겠습니까?")
                Text(text = "단어는 클라우드에 백업되며, 언제든지 앱으로 가져올 수 있습니다.")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun DownloadDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = "단어 복원하기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "단어를 복원하시겠습니까?")
                Text(text = "클라우드의 데이터를 기기로 복원합니다.")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
        onDismissRequest = onDismiss
    )
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() {
    val data = ProfileScreenData(
        user = UserImpl(uid = "dtd", username = "hsk", email = null, profileImageUrl = null)
    )
    MyVocaTheme {
        Content(
            data = data,
            onTryLogin = {},
            onLoginButtonClick = { _, _ -> },
            onLogout = {},
        )
    }
}