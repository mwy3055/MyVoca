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
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hsk.practice.myvoca.firebase.MyFirebaseAuth
import hsk.practice.myvoca.firebase.MyFirestore
import hsk.practice.myvoca.firebase.UserImpl
import hsk.practice.myvoca.work.setFirestoreDownloadWork
import hsk.practice.myvoca.work.setFirestoreUploadWork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _profileScreenData = MutableStateFlow(ProfileScreenData())
    val profileScreenData: StateFlow<ProfileScreenData>
        get() = _profileScreenData

    val profileFeatures = listOf(
        ProfileFeature(
            icon = Icons.Outlined.FileUpload,
            text = "단어 백업하기",
            onClick = {
                _profileScreenData.value = profileScreenData.value.copy(showUploadDialog = true)
            }
        ),
        ProfileFeature(
            icon = Icons.Outlined.FileDownload,
            text = "단어 복원하기",
            onClick = {
                _profileScreenData.value = profileScreenData.value.copy(showDownloadDialog = true)
            }
        )
    )

    init {
        MyFirebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            onLoginStateChange(user)

            // Check if backup data exists at the Firestore
            if (user != null) {
                viewModelScope.launch {
                    val backupDataPath = MyFirestore.backupDataReference(user.uid)
                    val exists = MyFirestore.collectionExists(backupDataPath)
                    _profileScreenData.value =
                        profileScreenData.value.copy(downloadPossible = exists)
                }
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

    fun onUploadConfirm() {
        scheduleUploadWork()
        hideUploadDialog()
    }

    fun onUploadDismiss() {
        hideUploadDialog()
    }

    private fun hideUploadDialog() {
        _profileScreenData.value = profileScreenData.value.copy(showUploadDialog = false)
    }

    fun onDownloadConfirm(onFailure: () -> Unit) {
        if (profileScreenData.value.downloadPossible == true) {
            scheduleDownloadWork()
        } else {
            onFailure()
        }
        hideDownloadDialog()
    }

    fun onDownloadDismiss() {
        hideDownloadDialog()
    }

    private fun hideDownloadDialog() {
        _profileScreenData.value = profileScreenData.value.copy(showDownloadDialog = false)
    }

    private fun scheduleUploadWork() {
        val user = profileScreenData.value.user ?: return
        setFirestoreUploadWork(workManager, user.uid!!)
    }

    private fun scheduleDownloadWork() {
        val user = profileScreenData.value.user ?: return
        setFirestoreDownloadWork(workManager, user.uid!!)
    }

}


data class ProfileScreenData(
    val user: UserImpl? = null,
    val showUploadDialog: Boolean = false,
    val showDownloadDialog: Boolean = false,
    val downloadPossible: Boolean? = null
)

data class ProfileFeature(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit
)