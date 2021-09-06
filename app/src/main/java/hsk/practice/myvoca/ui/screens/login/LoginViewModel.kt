package hsk.practice.myvoca.ui.screens.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _loginScreenData = MutableStateFlow(LoginScreenData())
    val loginScreenData: StateFlow<LoginScreenData>
        get() = _loginScreenData

    /* Event listeners for UI */
    fun onEmailChange(newEmail: String) {
        _loginScreenData.value = loginScreenData.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _loginScreenData.value = loginScreenData.value.copy(password = newPassword)
    }

    fun onTogglePasswordVisibility() {
        val current = loginScreenData.value.showPassword
        _loginScreenData.value = loginScreenData.value.copy(showPassword = !current)
    }

    fun onLogin() {
        // TODO: Email login!
    }


}

data class LoginScreenData(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
)