package com.asif.kmmauthorizedimageprofiles.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Koin
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        setContent {
            MyApplicationTheme {
                // Create an instance of SecurePreferences
                val securePreferences = remember { SecurePreferences(this) }

                // Setup NavHostController
                val navController = rememberNavController()

                // Determine the start destination based on token presence
                val startDestination = if (securePreferences.getToken().isNullOrEmpty()) {
                    "login"
                } else {
                    "home"
                }

                // Set up the navigation graph
                AppNavGraph(navController = navController, startDestination = startDestination, securePreferences = securePreferences)
            }
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String, securePreferences: SecurePreferences) {
    NavGraph(navController = navController, securePreferences = securePreferences, startDestination = startDestination)
}


