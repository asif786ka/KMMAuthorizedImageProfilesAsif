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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    // Obtain the ViewModel from Koin dependency injection
    val viewModel: ProfileViewModel = koinViewModel()

    // Collect the profile state as a Compose state
    val profileState by viewModel.profileState.collectAsState()

    // Create a CoroutineScope tied to the Composable's lifecycle
    val coroutineScope = rememberCoroutineScope()

    // State to hold the current image bitmap
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // State to manage the update button's progress indicator
    var isUpdating by remember { mutableStateOf(false) }

    // Obtain the current context
    val context = LocalContext.current

    // Launcher to pick an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageBitmap = viewModel.loadBitmapFromUri(it)
        }
    }

    // Launcher to take a picture with the camera
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imageBitmap = bitmap
    }

    Scaffold(
        topBar = {
            // Top AppBar with a back button
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
            // Main content of the Profile screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle different states of profile loading
                when (profileState) {
                    is ProfileState.Loading -> {
                        CircularProgressIndicator(color = Color.Black)
                    }
                    is ProfileState.Success -> {
                        ProfileCard(
                            profileState = profileState as ProfileState.Success,
                            profileImageBitmap = imageBitmap,
                            onImageBitmapChange = { newBitmap -> imageBitmap = newBitmap },
                            viewModel = viewModel
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Button to take a picture with the camera
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Take a Picture", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Button to pick an image from the gallery
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Pick from Gallery", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Button to update the avatar
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
                            // Show a progress indicator or "Update Avatar" text based on the updating state
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
                        // Show an error message if loading the profile fails
                        Toast.makeText(context, "Updating avatar failed", Toast.LENGTH_LONG).show()
                        Text("Error: ${(profileState as ProfileState.Error).message}", color = Color.Red)
                    }
                }
            }
        }
    )

    // Listen for profile state changes and show toast messages accordingly
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

    // Load the profile when the composable is first displayed
    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }
}

@Composable
fun ProfileCard(
    profileState: ProfileState.Success,
    profileImageBitmap: Bitmap?,
    onImageBitmapChange: (Bitmap?) -> Unit,
    viewModel: ProfileViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the user's email
            val profile = profileState.profile
            Text("Email: ${profile.email}", color = Color.Black)

            Spacer(modifier = Modifier.height(20.dp))

            // Display the profile image if available, or load a placeholder image
            if (profileImageBitmap != null) {
                Image(
                    bitmap = profileImageBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val placeholderUrl = "https://via.placeholder.com/200"
                val painter: Painter = rememberImagePainter(placeholderUrl)
                Image(
                    painter = painter,
                    contentDescription = "Placeholder Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Load the profile image asynchronously
                profile.avatar_url?.let { url ->
                    coroutineScope.launch {
                        val newBitmap = if (url.startsWith("data:image")) {
                            val base64Image = url.substringAfter("base64,")
                            val decodedByteArray = Base64.decode(base64Image, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
                        } else {
                            viewModel.loadBitmapFromUri(url.toUri())
                        }
                        onImageBitmapChange(newBitmap) // Reassign the mutable state variable through callback
                    }
                }
            }
        }
    }
}


