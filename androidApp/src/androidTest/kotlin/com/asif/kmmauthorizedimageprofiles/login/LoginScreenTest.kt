package com.asif.kmmauthorizedimageprofiles.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asif.kmmauthorizedimageprofiles.android.login.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_initialState() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("No account? Register here").assertExists()
    }

    @Test
    fun loginScreen_showsErrorDialog_onLoginFailure() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput("wrong@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Login").performClick()

        // Assuming the ViewModel returns a LoginState.Error
        composeTestRule.onNodeWithText("Login Error").assertExists()
    }

    @Test
    fun loginScreen_navigatesToHomeScreen_onLoginSuccess() {
        composeTestRule.setContent {
            LoginScreen(navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()

        // Check that the Home screen is displayed
        composeTestRule.onNodeWithText("Home Screen").assertExists()
    }
}
