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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hsk.practice.myvoca.firebase.MyFirebaseAuth
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.work.FirestoreUploadWordsWork
import hsk.practice.myvoca.work.setFirestoreDownloadWork
import hsk.practice.myvoca.work.setFirestoreUploadWork
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
        val uploadData = UploadFeatureData(
            onClick = onUploadClick,
            onConfirm = this::onUploadConfirm,
            onDismiss = this::onUploadDismiss
        )
        val downloadData = DownloadFeatureData(
            onClick = onDownloadClick,
            onConfirm = this::onDownloadConfirm,
            onDismiss = this::onDownloadDismiss
        )

        _profileScreenData.value = profileScreenData.value.copy(
            uploadFeatureData = uploadData,
            downloadFeatureData = downloadData
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
        if (profileScreenData.value.downloadFeatureData.downloadPossible == true) {
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
    val uploadFeatureData: UploadFeatureData = UploadFeatureData(),
    val downloadFeatureData: DownloadFeatureData = DownloadFeatureData()
) {
    /**
     * Copy method for [uploadFeatureData]
     */
    fun copy(
        showUploadDialog: Boolean = uploadFeatureData.showUploadDialog,
        uploadProgress: Float? = uploadFeatureData.uploadProgress
    ): ProfileScreenData {
        val newUploadData = uploadFeatureData.copy(
            showUploadDialog = showUploadDialog,
            uploadProgress = uploadProgress
        )
        return this.copy(uploadFeatureData = newUploadData)
    }

    /**
     * Copy method for [downloadFeatureData]
     */
    fun copy(
        showDownloadDialog: Boolean = downloadFeatureData.showDownloadDialog,
        downloadPossible: Boolean? = downloadFeatureData.downloadPossible
    ): ProfileScreenData {
        val newDownloadData = downloadFeatureData.copy(
            showDownloadDialog = showDownloadDialog,
            downloadPossible = downloadPossible
        )
        return this.copy(downloadFeatureData = newDownloadData)
    }
}

data class ProfileFeature(
    val icon: ImageVector,
    val text: String,
)

data class UploadFeatureData(
    val showUploadDialog: Boolean = false,
    val uploadProgress: Float? = null,
    val onClick: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {}
) {
    val featureData: ProfileFeature = ProfileFeature(
        icon = Icons.Outlined.FileUpload,
        text = "단어 백업하기"
    )

    val uploading: Boolean
        get() = uploadProgress != null
    val finished: Boolean
        get() = if (uploadProgress == null) false else uploadProgress >= 1f
}

data class DownloadFeatureData(
    val showDownloadDialog: Boolean = false,
    val downloadPossible: Boolean? = null,
    val onClick: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {}
) {
    val featureData: ProfileFeature = ProfileFeature(
        icon = Icons.Outlined.FileDownload,
        text = "단어 복원하기",
    )
}