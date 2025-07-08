package com.example.eventsityapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventsityapp.NavRoutes
import com.example.eventsityapp.data.model.EventStatus
import com.example.eventsityapp.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventListScreen(navController: NavController, viewModel: EventViewModel) {
    val events by viewModel.events.collectAsState()
    val eventState = viewModel.eventState
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getEvents()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "События") },
                    label = { Text("События") },
                    selected = true,
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "События твоего города",
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск событий") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                navController.navigate("event_search/$searchQuery")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                }
            )
            when (eventState) {
                is EventViewModel.EventState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is EventViewModel.EventState.Error -> {
                    Text(
                        text = (eventState as EventViewModel.EventState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    if (events.isEmpty()) {
                        Text(
                            text = "Нет событий",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn {
                            items(events) { event ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Город: ${event.city}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Место: ${event.location}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Дата: ${
                                                event.event_date?.let { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(event.event_date) } ?: "Дата не указана"
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Статус: ${
                                                when (event.status) {
                                                    EventStatus.DRAFT -> "Проект"
                                                    EventStatus.PUBLISHED -> "Размещен"
                                                    EventStatus.CANCELLED -> "Отменен"
                                                    EventStatus.COMPLETED -> "Прошел"
                                                }
                                            }",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        event.description?.let {
                                            Text(
                                                text = "Описание: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    navController.navigate(NavRoutes.EVENT_EDIT.replace("{eventId}", event.id.toString()))
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Редактировать")
                                            }
                                            Button(
                                                onClick = { viewModel.deleteEvent(event.id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("Удалить")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { navController.navigate(NavRoutes.EVENT_CREATE) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать событие")
            }
        }
    }
}