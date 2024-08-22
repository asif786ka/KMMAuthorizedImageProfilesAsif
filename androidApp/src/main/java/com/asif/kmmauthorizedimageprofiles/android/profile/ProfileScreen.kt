package com.asif.kmmauthorizedimageprofiles.android.profile

import android.graphics.Bitmap
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
                            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
                        } ?: run {
                            profile.avatar_url.let { url ->
                                coroutineScope.launch {
                                    val bitmap = viewModel.loadBitmapFromUri(url.toUri())
                                    imageBitmap = bitmap
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
                                    val compressedImage = viewModel.compressImage(bitmap)
                                    val base64Image = viewModel.encodeImageToBase64(compressedImage)
                                    coroutineScope.launch {
                                        viewModel.updateAvatar(base64Image)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Update Avatar", color = Color.White)
                        }
                    }
                    is ProfileState.Error -> {
                        Text("Error: ${(profileState as ProfileState.Error).message}", color = Color.Red)
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }
}
