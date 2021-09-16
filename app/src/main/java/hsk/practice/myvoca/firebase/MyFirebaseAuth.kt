package hsk.practice.myvoca.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import hsk.practice.myvoca.R

object MyFirebaseAuth {

    fun addAuthStateListener(@NonNull listener: FirebaseAuth.AuthStateListener) {
        Firebase.auth.addAuthStateListener(listener)
    }

    fun launchGoogleLogin(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        val webClientId = context.getString(R.string.web_client_id)
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, options)
        launcher.launch(googleSignInClient.signInIntent)
    }

    fun tryGoogleLogin(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            Logger.d("Account token: ${account.idToken!!}")
            credentialLogin(credential)
        } catch (e: ApiException) {
            Logger.w("Google sign in failed: $e")
        }
    }

    fun credentialLogin(credential: AuthCredential) {
        try {
            Firebase.auth.signInWithCredential(credential)
        } catch (e: Exception) {
            Logger.e("Login error: $e")
        }
    }

    fun logout() {
        Firebase.auth.signOut()
    }

}

data class UserImpl(
    val uid: String?,
    val username: String?,
    val email: String?,
    val profileImageUrl: Uri?
)