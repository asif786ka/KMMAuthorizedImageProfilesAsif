package com.asif.kmmauthorizedimageprofiles.android.home

import androidx.lifecycle.ViewModel
import com.asif.kmmauthorizedimageprofiles.android.SecurePreferences

class HomeViewModel(private val securePreferences: SecurePreferences) : ViewModel() {

    fun logout() {
        securePreferences.clearToken()
    }
}
