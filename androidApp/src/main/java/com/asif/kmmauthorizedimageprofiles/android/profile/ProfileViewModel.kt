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

    // MutableStateFlow to hold the current profile state
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> get() = _profileState

    init {
        // Load the profile immediately upon ViewModel initialization
        getProfile()
    }

    // Fetch the profile data from the repository
    fun getProfile() {
        val token = securePreferences.getToken()
        val userId = securePreferences.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                _profileState.value = ProfileState.Loading
                try {
                    // Fetch the profile using the repository
                    val profile = token?.let { repository.getProfile(userId, it) }
                    _profileState.value = profile?.let { ProfileState.Success(it) }!!
                } catch (e: Exception) {
                    // Handle errors by updating the state
                    _profileState.value = ProfileState.Error("Error loading profile: ${e.message}")
                }
            }
        } else {
            _profileState.value = ProfileState.Error("Invalid token")
        }
    }

    // Update the user's avatar by compressing and encoding the image
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAvatar(bitmap: Bitmap) {
        val token = securePreferences.getToken()
        val userId = securePreferences.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    // Compress and encode the image to Base64
                    val avatarBase64 = compressAndEncodeImage(bitmap, maxWidth = 800, maxHeight = 600, initialQuality = 75)
                    // Update the avatar using the repository
                    token?.let { repository.updateAvatar(userId, avatarBase64, it) }
                    // Refresh profile after updating avatar
                    getProfile()
                } catch (e: Exception) {
                    // Handle errors by updating the state
                    _profileState.value = ProfileState.Error("Error updating avatar: ${e.message}")
                }
            }
        } else {
            _profileState.value = ProfileState.Error("Invalid token")
        }
    }

    // Compress and encode the image to ensure it is under 1 MB
    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun compressAndEncodeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, initialQuality: Int = 100): String {
        // Calculate the scaling factor based on aspect ratio
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        var width = maxWidth
        var height = maxHeight
        if (bitmap.width > bitmap.height) {
            height = (width / aspectRatio).toInt()
        } else {
            width = (height * aspectRatio).toInt()
        }

        // Create a scaled bitmap to match the specified dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        var quality = initialQuality
        var encodedString: String
        do {
            val outputStream = ByteArrayOutputStream()
            // Compress the scaled bitmap to JPEG format
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            // Convert the byte array to Base64 string
            encodedString = Base64.getEncoder().encodeToString(byteArray)

            // Reduce the quality if the encoded string is too large
            quality -= 5

        } while (encodedString.toByteArray().size >= 1_000_000 && quality > 0) // Continue until the encoded string is under 1 MB

        // Prepend the necessary prefix to the Base64 string
        return "data:image/jpeg;base64,$encodedString"
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun compressAndEncodeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, initialQuality: Int = 100): String {
        // Scaling factor calculation and bitmap scaling remain the same
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        var width = maxWidth
        var height = maxHeight
        if (bitmap.width > bitmap.height) {
            height = (width / aspectRatio).toInt()
        } else {
            width = (height * aspectRatio).toInt()
        }

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        var quality = initialQuality
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var byteArray = outputStream.toByteArray()
        var encodedString = Base64.getEncoder().encodeToString(byteArray)

        // If the size is too large, calculate a closer estimate of the needed quality
        if (encodedString.toByteArray().size >= 1_000_000) {
            quality = (1_000_000f / encodedString.toByteArray().size * initialQuality).toInt()
            outputStream.reset()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            byteArray = outputStream.toByteArray()
            encodedString = Base64.getEncoder().encodeToString(byteArray)
        }

        return "data:image/jpeg;base64,$encodedString"
    }


    // Load a Bitmap from a given URI
    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Use ImageDecoder for newer versions of Android
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                // Use BitmapFactory for older versions
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: FileNotFoundException) {
            // Handle the case where the file was not found
            null
        } catch (e: Exception) {
            // Handle other potential errors
            null
        }
    }
}

// Sealed class to represent the different states of the profile
sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
