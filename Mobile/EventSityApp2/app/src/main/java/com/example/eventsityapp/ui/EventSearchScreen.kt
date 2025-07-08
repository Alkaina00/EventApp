package com.example.eventsityapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventsityapp.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSearchScreen(navController: NavController, viewModel: EventViewModel, initialQuery: String) {
    val searchResults by viewModel.searchResults.collectAsState()
    val eventState = viewModel.eventState
    var searchQuery by remember { mutableStateOf(initialQuery) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchEvents(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск событий") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск событий") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchEvents(searchQuery)
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
                    if (searchResults.isEmpty()) {
                        Text(
                            text = "Нет результатов поиска",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn {
                            items(searchResults) { event ->
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
                                            text = "Статус: Размещен",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        event.description?.let {
                                            Text(
                                                text = "Описание: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}