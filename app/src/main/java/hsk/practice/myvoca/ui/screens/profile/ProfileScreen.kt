package hsk.practice.myvoca.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val data by viewModel.profileScreenData.collectAsState()

    ProfileContent(
        data = data,
        onTryLogin = viewModel::onTryLogin,
        onLoginButtonClick = viewModel::onLoginButtonClick,
        onLogout = viewModel::onLogout,
        profileFeatures = viewModel.profileFeatures,
        onUploadConfirm = viewModel::onUploadConfirm,
        onUploadDismiss = viewModel::onUploadDismiss,
        onDownloadConfirm = viewModel::onDownloadConfirm,
        onDownloadDismiss = viewModel::onDownloadDismiss
    )
}

@Composable
private fun ProfileContent(
    data: ProfileScreenData,
    onTryLogin: (ActivityResult) -> Unit,
    onLoginButtonClick: (Context, ManagedActivityResultLauncher<Intent, ActivityResult>) -> Unit,
    onLogout: () -> Unit,
    profileFeatures: List<ProfileFeature>,
    onUploadConfirm: () -> Unit,
    onUploadDismiss: () -> Unit,
    onDownloadConfirm: (() -> Unit) -> Unit,
    onDownloadDismiss: () -> Unit,
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
        }
        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        ProfileFeatures(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            user = user,
            features = profileFeatures,
        )
        Spacer(modifier = Modifier.weight(3f))
    }

    if (data.showUploadDialog) {
        ProfileUploadDialog(onConfirm = onUploadConfirm, onDismiss = onUploadDismiss)
    }
    if (data.showDownloadDialog) {
        ProfileDownloadDialog(onConfirm = onDownloadConfirm, onDismiss = onDownloadDismiss)
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ProfileImage(
    modifier: Modifier = Modifier,
    imageUrl: Uri?
) {
    val imageSize = 150
    val imagePainter = if (imageUrl == null) {
        rememberVectorPainter(image = Icons.Outlined.HelpOutline)
    } else {
        rememberImagePainter(
            data = imageUrl,
            builder = {
                size(imageSize)
                transformations(CircleCropTransformation())
            }
        )
    }
    Image(
        modifier = modifier.size(imageSize.dp),
        painter = imagePainter,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileFeatures(
    modifier: Modifier = Modifier,
    user: UserImpl?,
    features: List<ProfileFeature>
) {
    val clickable = user != null
    LazyVerticalGrid(cells = GridCells.Fixed(3), modifier = modifier) {
        items(features) { feature ->
            ProfileFeatureItem(
                feature = feature,
                enabled = clickable
            )
        }
    }

}

@Composable
private fun ProfileFeatureItem(
    feature: ProfileFeature,
    enabled: Boolean
) {
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = feature.onClick)
            .aspectRatio(1f)
            .alpha(alpha),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = feature.text,
            modifier = Modifier.fillMaxSize(0.4f)
        )
        Text(text = feature.text)
    }
}

@Composable
private fun ProfileUploadDialog(
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
private fun ProfileDownloadDialog(
    onConfirm: (() -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        title = { Text(text = "단어 복원하기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "단어를 복원하시겠습니까?")
                Text(text = "클라우드의 데이터를 기기로 복원합니다.")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm {
                    // When there is no data to restore
                    Toast.makeText(context, "가져올 데이터가 없습니다.", Toast.LENGTH_LONG).show()
                }
            }) {
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
private fun ProfileContentPreview() {
    val data = ProfileScreenData(
        user = UserImpl(uid = "dtd", username = "hsk", email = null, profileImageUrl = null)
    )
    MyVocaTheme {
        ProfileContent(
            data = data,
            onTryLogin = {},
            onLoginButtonClick = { _, _ -> },
            onLogout = {},
            profileFeatures = listOf(
                ProfileFeature(
                    icon = Icons.Outlined.FileUpload,
                    text = "단어 백업하기",
                    onClick = { }
                ),
                ProfileFeature(
                    icon = Icons.Outlined.FileDownload,
                    text = "단어 복원하기",
                    onClick = { }
                )
            ),
            onUploadConfirm = {},
            onUploadDismiss = {},
            onDownloadConfirm = {},
            onDownloadDismiss = {}
        )
    }
}