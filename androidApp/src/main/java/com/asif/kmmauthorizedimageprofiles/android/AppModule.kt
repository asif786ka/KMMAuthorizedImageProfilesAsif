package com.asif.kmmauthorizedimageprofiles.android

import com.asif.kmmauthorizedimageprofiles.ApiService
import com.asif.kmmauthorizedimageprofiles.UserRepository
import com.asif.kmmauthorizedimageprofiles.android.home.HomeViewModel
import com.asif.kmmauthorizedimageprofiles.android.login.LoginViewModel
import com.asif.kmmauthorizedimageprofiles.android.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Singletons
    single { ApiService() }
    single { SecurePreferences(get()) }
    single { UserRepository(get()) }

    // ViewModels
    viewModel { LoginViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
}
