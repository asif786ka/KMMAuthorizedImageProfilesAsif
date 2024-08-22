package com.asif.kmmauthorizedimageprofiles.android.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asif.kmmauthorizedimageprofiles.TokenResponse
import com.asif.kmmauthorizedimageprofiles.UserRepository
import com.asif.kmmauthorizedimageprofiles.android.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val tokenResponse: TokenResponse) : LoginState()
    data class Error(val message: String) : LoginState() // Updated to use a string message for simplicity
}

class LoginViewModel(
    private val repository: UserRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.register(email, password)
                if (response != null) {
                    securePreferences.saveToken(response.token)
                    _loginState.value = LoginState.Success(response)
                } else {
                    _loginState.value = LoginState.Error("Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(email, password)
                if (response != null) {
                    securePreferences.saveToken(response.token)
                    _loginState.value = LoginState.Success(response)
                } else {
                    _loginState.value = LoginState.Error("Invalid email or password. Please try again.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}


