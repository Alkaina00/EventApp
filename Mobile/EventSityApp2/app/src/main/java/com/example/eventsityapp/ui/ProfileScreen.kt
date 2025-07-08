package com.example.eventsityapp.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.eventsityapp.NavRoutes
import com.example.eventsityapp.viewmodel.AuthViewModel
import com.example.eventsityapp.viewmodel.EventViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, eventViewModel: EventViewModel, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val authState by authViewModel::authState
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var phone by remember { mutableStateOf(userProfile?.phone ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Обновляем поля при изменении профиля
    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            phone = it.phone ?: ""
            selectedImageUri = null
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.getUserProfile()
    }

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.LoggedOut) {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    // Лаунчер для выбора изображения
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль пользователя") },
                actions = {
                    if (!isEditing && authState is AuthViewModel.AuthState.Success) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "События") },
                    label = { Text("События") },
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.EVENT_LIST) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                    label = { Text("Профиль") },
                    selected = true,
                    onClick = { navController.navigate(NavRoutes.PROFILE) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (authState) {
                is AuthViewModel.AuthState.Loading -> {
                    CircularProgressIndicator()
                }
                is AuthViewModel.AuthState.Error -> {
                    Text(
                        text = (authState as AuthViewModel.AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is AuthViewModel.AuthState.Success -> {
                    userProfile?.let { profile ->
                        // Отображение фотографии профиля
                        if (isEditing && selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Фотография профиля",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else if (profile.profilePhoto != null) {
                            Image(
                                painter = rememberAsyncImagePainter("http://10.0.2.2:3001${profile.profilePhoto}"),
                                contentDescription = "Фотография профиля",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            // Заглушка, если фото нет
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Фотография профиля отсутствует",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isEditing) {
                            // Режим редактирования
                            Button(
                                onClick = {
                                    pickImageLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Выбрать фотографию")
                            }
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Имя") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = name.isBlank()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Телефон (опционально)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (name.isNotBlank()) {
                                            val photoPart = selectedImageUri?.let { uri ->
                                                createMultipartBodyPart(context, uri)
                                            }
                                            authViewModel.updateProfileWithPhoto(name, phone.ifBlank { null }, photoPart)
                                            isEditing = false
                                            selectedImageUri = null
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = name.isNotBlank()
                                ) {
                                    Text("Сохранить")
                                }
                                Button(
                                    onClick = {
                                        isEditing = false
                                        selectedImageUri = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Отмена")
                                }
                            }
                        } else {
                            // Режим просмотра
                            Text("Имя: ${profile.name}")
                            Text("Email: ${profile.email}")
                            profile.phone?.let { phone ->
                                Text("Телефон: $phone")
                            } ?: Text("Телефон: не указан")
                        }
                    } ?: Text("Профиль не загружен")
                }
                is AuthViewModel.AuthState.LoggedOut -> {
                    // Ничего не отображаем, так как навигация уже произошла
                }
                else -> {
                    Text("Загрузка профиля...")
                }
            }

            if (!isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}

// Утилита для создания MultipartBody.Part из Uri
fun createMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
        file.deleteOnExit() // Удаляем файл при завершении работы JVM
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("photo", file.name, requestFile)
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Failed to create multipart body part", e)
        null
    }
}