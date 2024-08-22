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
import java.util.Base64

class ProfileViewModel(private val repository: UserRepository, private val securePreferences: SecurePreferences, private val context: Context) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> get() = _profile

    fun getProfile() {
        val userId = securePreferences.getToken()?.let { parseUserIdFromToken(it) }
        userId?.let {
            viewModelScope.launch {
                val profile = repository.getProfile(it, securePreferences.getToken()!!)
                _profile.value = profile
            }
        }
    }

    fun updateAvatar(avatarBase64: String) {
        val userId = securePreferences.getToken()?.let { parseUserIdFromToken(it) }
        userId?.let {
            viewModelScope.launch {
                repository.updateAvatar(it, avatarBase64, securePreferences.getToken()!!)
                getProfile()
            }
        }
    }

    fun compressImage(bitmap: Bitmap): Bitmap {
        // Compress the image to reduce its size while maintaining quality
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    private fun parseUserIdFromToken(token: String): String {
        // Extract the user ID from the token
        return "extracted_user_id"
    }
}
