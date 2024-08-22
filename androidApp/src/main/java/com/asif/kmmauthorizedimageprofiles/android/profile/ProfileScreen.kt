package com.asif.kmmauthorizedimageprofiles.android.profile

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(navController: NavHostController) {
    val viewModel: ProfileViewModel = koinViewModel()
    val profile by viewModel.profile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageBitmap = viewModel.loadBitmapFromUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imageBitmap = bitmap
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        profile?.let {
            Text("Email: ${it.email}")
            Spacer(modifier = Modifier.height(20.dp))
            imageBitmap?.let { bitmap ->
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { cameraLauncher.launch(null) }) {
                Text("Take a Picture")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Pick from Gallery")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                imageBitmap?.let { bitmap ->
                    val compressedImage = viewModel.compressImage(bitmap)
                    val base64Image = viewModel.encodeImageToBase64(compressedImage)
                    coroutineScope.launch {
                        viewModel.updateAvatar(base64Image)
                    }
                }
            }) {
                Text("Update Avatar")
            }
        } ?: run {
            Text("Loading profile...")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }
}
