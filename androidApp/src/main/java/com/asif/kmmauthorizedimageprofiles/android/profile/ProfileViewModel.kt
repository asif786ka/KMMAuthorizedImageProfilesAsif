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

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAvatar(bitmap: Bitmap) {
        val token = securePreferences.getToken()
        val userId = securePreferences.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    // Compress and encode the image to Base64
                    val avatarBase64 = compressAndEncodeImage(bitmap, maxWidth = 800, maxHeight = 600, initialQuality = 75)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun compressAndEncodeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, initialQuality: Int = 100): String {
        // Calculate the scaling factor
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        var width = maxWidth
        var height = maxHeight
        if (bitmap.width > bitmap.height) {
            height = (width / aspectRatio).toInt()
        } else {
            width = (height * aspectRatio).toInt()
        }

        // Create a scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        var quality = initialQuality
        var encodedString: String
        do {
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            // Convert to Base64 string
            encodedString = Base64.getEncoder().encodeToString(byteArray)

            // Reduce the quality
            quality -= 5

        } while (encodedString.toByteArray().size >= 1_000_000 && quality > 0) // Continue until under 1 MB or quality is too low

        // Prepend the necessary prefix to the Base64 string
        return "data:image/jpeg;base64,$encodedString"
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
