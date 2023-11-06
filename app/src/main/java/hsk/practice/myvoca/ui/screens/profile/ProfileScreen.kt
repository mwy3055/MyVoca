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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import hsk.practice.myvoca.R
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.ui.components.MyVocaText
import hsk.practice.myvoca.ui.theme.MyVocaTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val data by viewModel.profileScreenData.collectAsStateWithLifecycle()

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
                    username = user.username ?: stringResource(R.string.unknown_name),
                    email = user.email ?: stringResource(R.string.unknown_email),
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
            contentDescription = stringResource(R.string.you_are_not_logged_in_yet),
            tint = MaterialTheme.colorScheme.onBackground,
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
            contentDescription = stringResource(R.string.profile_image),
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
        MyVocaText(text = stringResource(R.string.logout))
    }
}

@Composable
private fun UserEmail(email: String) {
    MyVocaText(
        text = email,
        style = MaterialTheme.typography.displayLarge
    )
}

@Composable
private fun Username(username: String) {
    MyVocaText(
        text = username,
        style = MaterialTheme.typography.headlineSmall
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
        MyVocaText(text = stringResource(R.string.login))
    }
}

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
            data.uploading -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onBackground
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
                contentDescription = actionData.text.asString(),
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
        MyVocaText(text = actionData.text.asString())
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
            contentDescription = actionData.text.asString(),
            modifier = Modifier.fillMaxSize(fraction = actionIconFraction)
        )
        MyVocaText(text = actionData.text.asString())
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
        title = { MyVocaText(text = stringResource(R.string.back_up_your_words)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MyVocaText(text = stringResource(R.string.do_you_warnt_to_back_up_the_words))
                MyVocaText(text = stringResource(R.string.words_are_backed_up_to_the_cloud_and_can_be_imported_into_the_app_at_any_time))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                MyVocaText(text = stringResource(id = R.string.check))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                MyVocaText(text = stringResource(R.string.cancellation))
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
        title = { MyVocaText(text = stringResource(R.string.restore_words)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MyVocaText(text = stringResource(R.string.would_you_like_to_restore_the_word))
                MyVocaText(text = stringResource(R.string.restore_data_from_the_cloud_to_your_device))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                MyVocaText(text = stringResource(id = R.string.check))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                MyVocaText(text = stringResource(id = R.string.cancellation))
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