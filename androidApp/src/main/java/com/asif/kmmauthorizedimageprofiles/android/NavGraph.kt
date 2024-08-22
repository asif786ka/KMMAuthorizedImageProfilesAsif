package com.asif.kmmauthorizedimageprofiles.android

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.asif.kmmauthorizedimageprofiles.android.home.HomeScreen
import com.asif.kmmauthorizedimageprofiles.android.login.LoginScreen
import com.asif.kmmauthorizedimageprofiles.android.profile.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController, securePreferences: SecurePreferences, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}

