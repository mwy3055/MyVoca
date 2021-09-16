package hsk.practice.myvoca.ui.screens.profile

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import hsk.practice.myvoca.firebase.MyFirebaseAuth
import hsk.practice.myvoca.firebase.UserImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _profileScreenData = MutableStateFlow(ProfileScreenData())
    val profileScreenData: StateFlow<ProfileScreenData>
        get() = _profileScreenData

    init {
        MyFirebaseAuth.addAuthStateListener { auth ->
            onLoginStateChange(auth.currentUser)
        }
    }

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

}


data class ProfileScreenData(
    val user: UserImpl? = null
)

