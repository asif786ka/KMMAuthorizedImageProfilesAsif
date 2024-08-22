package com.asif.kmmauthorizedimageprofiles.android.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val viewModel: ProfileViewModel = koinViewModel()
    val profileState by viewModel.profileState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isUpdating by remember { mutableStateOf(false) }  // State to manage button content
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageBitmap = viewModel.loadBitmapFromUri(it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imageBitmap = bitmap
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (profileState) {
                    is ProfileState.Loading -> {
                        CircularProgressIndicator(color = Color.Black)
                    }
                    is ProfileState.Success -> {
                        val profile = (profileState as ProfileState.Success).profile
                        Text("Email: ${profile.email}", color = Color.Black)

                        imageBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(100.dp)
                            )
                        } ?: run {
                            profile.avatar_url?.let { url ->
                                coroutineScope.launch {
                                    imageBitmap = if (url.startsWith("data:image")) {
                                        // Decode Base64 image
                                        val base64Image = url.substringAfter("base64,")
                                        val decodedByteArray = Base64.decode(base64Image, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
                                    } else {
                                        // Load from URL
                                        viewModel.loadBitmapFromUri(url.toUri())
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Take a Picture", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Pick from Gallery", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                imageBitmap?.let { bitmap ->
                                    isUpdating = true  // Show progress indicator in the button
                                    coroutineScope.launch {
                                        viewModel.updateAvatar(bitmap)
                                        isUpdating = false  // Hide progress indicator after the operation
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)  // Adjust size as needed
                                )
                            } else {
                                Text("Update Avatar", color = Color.White)
                            }
                        }
                    }
                    is ProfileState.Error -> {
                        Toast.makeText(context, "Updating avatar failed", Toast.LENGTH_LONG).show()
                        Text("Error: ${(profileState as ProfileState.Error).message}", color = Color.Red)
                    }
                }
            }
        }
    )

    // Listen for update avatar state and show toast messages accordingly
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                Toast.makeText(context, "Avatar updated successfully", Toast.LENGTH_LONG).show()
            }
            is ProfileState.Error -> {
                Toast.makeText(context, "Updating avatar failed", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }
}

