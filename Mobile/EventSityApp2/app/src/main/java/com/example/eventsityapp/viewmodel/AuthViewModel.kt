package com.example.eventsityapp.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsityapp.data.api.AuthApi
import com.example.eventsityapp.data.api.AuthRequest
import com.example.eventsityapp.data.api.UserProfile
import com.example.eventsityapp.data.api.UpdateProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AuthViewModel(private val authApi: AuthApi, val context: Context) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    var authState: AuthState by mutableStateOf(AuthState.Idle)
        private set

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
        object LoggedOut : AuthState()
    }

    fun register(email: String, password: String, name: String, phone: String?) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val response = authApi.register(AuthRequest(email, password))
                context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("jwt_token", response.token)
                    .putString("userId", response.userId)
                    .apply()
                authState = AuthState.Success
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val response = authApi.login(AuthRequest(email, password))
                context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("jwt_token", response.token)
                    .putString("userId", response.userId)
                    .apply()
                authState = AuthState.Success
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Ошибка авторизации")
            }
        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null) ?: throw Exception("Токен не найден")
                val profile = authApi.getProfile("Bearer $token")
                _userProfile.value = profile
                authState = AuthState.Success
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Ошибка загрузки профиля")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null) ?: throw Exception("Токен не найден")
                        authApi.logout("Bearer $token")
                        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                _userProfile.value = null
                authState = AuthState.LoggedOut
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Ошибка при выходе")
            }
        }
    }

    fun updateProfileWithPhoto(name: String, phone: String?, photo: MultipartBody.Part?) {
        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null) ?: throw Exception("Токен не найден")
                val updatedProfile = authApi.updateProfileWithPhoto("Bearer $token", name, phone, photo)
                _userProfile.value = updatedProfile
                authState = AuthState.Success
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Ошибка обновления профиля")
            }
        }
    }
}