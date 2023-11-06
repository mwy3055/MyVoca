package hsk.practice.myvoca.ui.screens.profile

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseUser
import com.hsk.ktx.equalsDelta
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hsk.practice.myvoca.R
import hsk.practice.myvoca.firebase.MyFirebaseAuth
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.util.UiText
import hsk.practice.myvoca.work.FirestoreUploadWordsWork
import hsk.practice.myvoca.work.setFirestoreDownloadWork
import hsk.practice.myvoca.work.setFirestoreUploadWork
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _profileScreenData = MutableStateFlow(ProfileScreenData())

    val profileScreenData: StateFlow<ProfileScreenData>
        get() = _profileScreenData

    init {
        val onUploadClick = {
            _profileScreenData.value = profileScreenData.value.copy(showUploadDialog = true)
        }
        val onDownloadClick = {
            _profileScreenData.value = profileScreenData.value.copy(showDownloadDialog = true)
        }
        val uploadData = UploadActionData(
            onClick = onUploadClick,
            onConfirm = this::onUploadConfirm,
            onDismiss = this::onUploadDismiss
        )
        val downloadData = DownloadActionData(
            onClick = onDownloadClick,
            onConfirm = this::onDownloadConfirm,
            onDismiss = this::onDownloadDismiss
        )

        _profileScreenData.value = profileScreenData.value.copy(
            uploadActionData = uploadData,
            downloadActionData = downloadData
        )

        MyFirebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            onLoginStateChange(user)

            // Check if backup data exists at the Firestore
            if (user != null) {
                viewModelScope.launch {
                    repeat(10000) {
                        val backupDataPath = MyFirestore.backupDataReference(user.uid)
                        val exists = MyFirestore.collectionExists(backupDataPath)
                        _profileScreenData.value =
                            profileScreenData.value.copy(downloadPossible = exists)
                        delay(1000L)
                    }
                }
            } else {
                _profileScreenData.value = profileScreenData.value.copy(downloadPossible = false)
            }
        }
    }

    /* Event listeners for Compose UI */
    fun onLoginButtonClick(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) = MyFirebaseAuth.launchGoogleLogin(context, launcher)

    fun onTryLogin(result: ActivityResult) = MyFirebaseAuth.tryGoogleLogin(result)

    private fun onLoginStateChange(user: FirebaseUser?) {
        val userData = if (user != null) {
            UserImpl(
                uid = user.uid,
                username = user.displayName,
                email = user.email,
                profileImageUrl = user.photoUrl
            )
        } else {
            null
        }
        _profileScreenData.value = profileScreenData.value.copy(user = userData)
    }

    fun onLogout() = MyFirebaseAuth.logout()

    private fun onUploadConfirm() {
        _profileScreenData.value = profileScreenData.value.copy(uploadProgress = 0f)
        scheduleUploadWork()
        hideUploadDialog()
    }

    private fun onUploadDismiss() {
        hideUploadDialog()
    }

    private fun hideUploadDialog() {
        _profileScreenData.value = profileScreenData.value.copy(showUploadDialog = false)
    }

    private fun onDownloadConfirm() {
        if (profileScreenData.value.downloadActionData.downloadPossible == true) {
            scheduleDownloadWork()
        }
        hideDownloadDialog()
    }

    private fun onDownloadDismiss() {
        hideDownloadDialog()
    }

    private fun hideDownloadDialog() {
        _profileScreenData.value = profileScreenData.value.copy(showDownloadDialog = false)
    }

    private fun scheduleUploadWork() {
        val user = profileScreenData.value.user ?: return
        val uuid = setFirestoreUploadWork(workManager, user.uid!!)
        trackProgress(uuid)
    }

    private fun scheduleDownloadWork() {
        val user = profileScreenData.value.user ?: return
        setFirestoreDownloadWork(workManager, user.uid!!)
    }

    private fun trackProgress(workId: UUID) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdLiveData(workId).asFlow().collect { workInfo ->
                // 연속으로 업로드할 경우 이전의 workInfo를 찾을 수 없어 NPE가 발생한다.
                val progress = try {
                    workInfo.progress.getFloat(FirestoreUploadWordsWork.progressKey, 0f)
                } catch (e: NullPointerException) {
                    0f
                }
                _profileScreenData.value = profileScreenData.value.copy(uploadProgress = progress)
                if (progress >= 1f) {
                    workManager.cancelWorkById(workId)
                }
            }
        }
    }
}

data class ProfileScreenData(
    val user: UserImpl? = null,
    val uploadActionData: UploadActionData = UploadActionData(),
    val downloadActionData: DownloadActionData = DownloadActionData()
) {
    fun copy(
        showUploadDialog: Boolean = uploadActionData.showUploadDialog,
        uploadProgress: Float? = uploadActionData.uploadProgress
    ): ProfileScreenData {
        val newUploadData = uploadActionData.copy(
            showUploadDialog = showUploadDialog,
            uploadProgress = uploadProgress
        )
        return this.copy(uploadActionData = newUploadData)
    }

    fun copy(
        showDownloadDialog: Boolean = downloadActionData.showDownloadDialog,
        downloadPossible: Boolean? = downloadActionData.downloadPossible
    ): ProfileScreenData {
        val newDownloadData = downloadActionData.copy(
            showDownloadDialog = showDownloadDialog,
            downloadPossible = downloadPossible
        )
        return this.copy(downloadActionData = newDownloadData)
    }
}

data class ProfileAction(
    val icon: ImageVector,
    val text: UiText
)

data class UploadActionData(
    val showUploadDialog: Boolean = false,
    val uploadProgress: Float? = null,
    val onClick: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {}
) {
    val actionData: ProfileAction = ProfileAction(
        icon = Icons.Outlined.FileUpload,
        text = UiText.StringResource(R.string.back_up_word)
    )

    val uploading: Boolean
        get() = uploadProgress != null
    val finished: Boolean
        get() = uploadProgress?.equalsDelta(1f) ?: false
}

data class DownloadActionData(
    val showDownloadDialog: Boolean = false,
    val downloadPossible: Boolean? = null,
    val onClick: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {}
) {
    val actionData: ProfileAction = ProfileAction(
        icon = Icons.Outlined.FileDownload,
        text = UiText.StringResource(R.string.restore_word),
    )
}