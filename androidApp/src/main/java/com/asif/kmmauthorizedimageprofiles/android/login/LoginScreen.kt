package com.asif.kmmauthorizedimageprofiles.android.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
) {
    val viewModel: LoginViewModel = koinViewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistered by remember { mutableStateOf(true) } // Assuming user is registered by default

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    // Reset the fields and state
                    email = ""
                    password = ""
                    viewModel.resetState()
                }) {
                    Text("OK")
                }
            },
            title = { Text(text = "Login Error") },
            text = { Text(text = errorMessage) }
        )
    }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val internetPermissionState = rememberPermissionState(android.Manifest.permission.INTERNET)

    val state by viewModel.loginState.collectAsState()

    when (state) {
        is LoginState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
        }
        is LoginState.Success -> {
            LaunchedEffect(Unit) {
                navController.navigate("home")
            }
        }
        is LoginState.Error -> {
            errorMessage = (state as LoginState.Error).message
            showErrorDialog = true
        }
        else -> {
            // UI code for login form with buttons for login and register
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (cameraPermissionState.status.isGranted && internetPermissionState.status.isGranted) {
                            if (isRegistered) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.register(email, password)
                            }
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                            internetPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isRegistered) "Login" else "Register")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { isRegistered = !isRegistered },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isRegistered) "No account? Register here" else "Already registered? Login here")
                }
            }
        }
    }
}
