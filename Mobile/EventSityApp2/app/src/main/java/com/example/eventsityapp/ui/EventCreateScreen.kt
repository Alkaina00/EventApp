package com.example.eventsityapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventsityapp.NavRoutes
import com.example.eventsityapp.data.model.EventStatus
import com.example.eventsityapp.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateScreen(navController: NavController, viewModel: EventViewModel, eventId: Int? = null) {
    val events by viewModel.events.collectAsState()
    val event = eventId?.let { id -> events.find { it.id == id } }
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var city by remember { mutableStateOf(event?.city ?: "") }
    var status by remember { mutableStateOf(event?.status ?: EventStatus.DRAFT) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(event?.event_date ?: Date()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )
    val isEditing = event != null
    val cities = listOf(
        "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Казань",
        "Нижний Новгород", "Челябинск", "Самара", "Омск", "Ростов-на-Дону"
    )
    var expandedCity by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать событие" else "Создать событие") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.PROFILE) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = expandedCity,
                onExpandedChange = { expandedCity = !expandedCity }
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = {},
                    label = { Text("Город") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) }
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false }
                ) {
                    cities.forEach { cityOption ->
                        DropdownMenuItem(
                            text = { Text(cityOption) },
                            onClick = {
                                city = cityOption
                                expandedCity = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Место") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = when (status) {
                        EventStatus.DRAFT -> "Проект"
                        EventStatus.PUBLISHED -> "Размещен"
                        EventStatus.CANCELLED -> "Отменен"
                        EventStatus.COMPLETED -> "Прошел"
                    },
                    onValueChange = {},
                    label = { Text("Статус") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) }
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    EventStatus.values().forEach { statusOption ->
                        DropdownMenuItem(
                            text = { Text(
                                when (statusOption) {
                                    EventStatus.DRAFT -> "Проект"
                                    EventStatus.PUBLISHED -> "Размещен"
                                    EventStatus.CANCELLED -> "Отменен"
                                    EventStatus.COMPLETED -> "Прошел"
                                }
                            ) },
                            onClick = {
                                status = statusOption
                                expandedStatus = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(selectedDate),
                onValueChange = { },
                label = { Text("Дата и время") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                    }
                }
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                selectedDate = Date(selectedMillis)
                            }
                        }) {
                            Text("ОК")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена")
                        }
                    },
                    content = {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && location.isNotBlank() && city.isNotBlank()) {
                        if (isEditing) {
                            event?.id?.let { id ->
                                viewModel.updateEvent(id, title, description, selectedDate, location, city, status)
                            }
                        } else {
                            viewModel.createEvent(title, description, selectedDate, location, city, status)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Обновить" else "Сохранить")
            }
            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        event?.id?.let { viewModel.deleteEvent(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            }
            when (val state = viewModel.eventState) {
                is EventViewModel.EventState.Loading -> CircularProgressIndicator()
                is EventViewModel.EventState.Success -> {
                    LaunchedEffect(state) {
                        navController.navigate(NavRoutes.EVENT_LIST) {
                            popUpTo(NavRoutes.EVENT_LIST) { inclusive = true }
                        }
                    }
                }
                is EventViewModel.EventState.Deleted -> {
                    LaunchedEffect(state) {
                        navController.navigate(NavRoutes.EVENT_LIST) {
                            popUpTo(NavRoutes.EVENT_LIST) { inclusive = true }
                        }
                    }
                }
                is EventViewModel.EventState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {}
            }
        }
    }
}