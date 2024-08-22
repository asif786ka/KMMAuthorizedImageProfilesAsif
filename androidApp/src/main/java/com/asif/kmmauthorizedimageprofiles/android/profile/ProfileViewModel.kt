package com.asif.kmmauthorizedimageprofiles.android.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asif.kmmauthorizedimageprofiles.ProfileResponse
import com.asif.kmmauthorizedimageprofiles.UserRepository
import com.asif.kmmauthorizedimageprofiles.android.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.Base64

class ProfileViewModel(
    private val repository: UserRepository,
    private val securePreferences: SecurePreferences,
    private val context: Context
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> get() = _profileState

    init {
        // Load the profile immediately upon ViewModel initialization
        getProfile()
    }

    fun getProfile() {
        val token = securePreferences.getToken()
        val userId = securePreferences.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                _profileState.value = ProfileState.Loading
                try {
                    val profile = token?.let { repository.getProfile(userId, it) }
                    _profileState.value = profile?.let { ProfileState.Success(it) }!!
                } catch (e: Exception) {
                    _profileState.value = ProfileState.Error("Error loading profile: ${e.message}")
                }
            }
        } else {
            _profileState.value = ProfileState.Error("Invalid token")
        }
    }

    fun updateAvatar(avatarBase64: String) {
        val token = securePreferences.getToken()
        val userId = securePreferences.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    token?.let { repository.updateAvatar(userId, avatarBase64, it) }
                    getProfile()  // Refresh profile after updating avatar
                } catch (e: Exception) {
                    _profileState.value = ProfileState.Error("Error updating avatar: ${e.message}")
                }
            }
        } else {
            _profileState.value = ProfileState.Error("Invalid token")
        }
    }

    fun compressImage(bitmap: Bitmap): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: FileNotFoundException) {
            //logger.e(e) { "File not found for URI: $uri" }
            null
        } catch (e: Exception) {
            //logger.e(e) { "Error loading bitmap from URI: $uri" }
            null
        }
    }


    private fun parseUserIdFromToken(token: String): String {
        // Logic to extract the user ID from the token, placeholder function
        return "extracted_user_id"
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

