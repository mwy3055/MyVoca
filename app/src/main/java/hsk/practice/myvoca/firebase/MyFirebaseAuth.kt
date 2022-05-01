package hsk.practice.myvoca.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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
        val googleSignInIntent = getGoogleSignInIntent(context)
        launcher.launch(googleSignInIntent)
    }

    private fun getGoogleSignInIntent(context: Context): Intent {
        val googleSignInClient = getGoogleSignInClient(context)
        return googleSignInClient.signInIntent
    }

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = context.getString(R.string.web_client_id)
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun tryGoogleLogin(result: ActivityResult) {
        val task = getSignedInAccount(result)
        try {
            googleLogin(task)
        } catch (e: Exception) {
            onGoogleLoginFail(e)
        }
    }

    private fun getSignedInAccount(result: ActivityResult): Task<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(result.data)
    }

    private fun googleLogin(task: Task<GoogleSignInAccount>) {
        val credential = getGoogleAuthCredential(task)
        loginWithCredential(credential)
    }

    private fun getGoogleAuthCredential(task: Task<GoogleSignInAccount>): AuthCredential {
        val account = task.getResult(ApiException::class.java)!!
        return GoogleAuthProvider.getCredential(account.idToken!!, null)
    }

    fun loginWithCredential(credential: AuthCredential) {
        Firebase.auth.signInWithCredential(credential)
    }

    private fun onGoogleLoginFail(e: Exception) {
        Logger.w("Google sign in failed: $e")
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