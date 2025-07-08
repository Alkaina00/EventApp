package com.example.eventsityapp.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventsityapp.data.api.EventApi
import com.example.eventsityapp.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class EventViewModel(private val eventApi: EventApi, private val context: Context) : ViewModel() {
    var eventState by mutableStateOf<EventState>(EventState.Idle)
        private set
    private val _events = MutableStateFlow<List<EventResponse>>(emptyList())
    val events: StateFlow<List<EventResponse>> = _events
    private val _searchResults = MutableStateFlow<List<EventResponse>>(emptyList())
    val searchResults: StateFlow<List<EventResponse>> = _searchResults

    sealed class EventState {
        object Idle : EventState()
        object Loading : EventState()
        data class Success(val event: EventResponse? = null) : EventState()
        data class Error(val message: String) : EventState()
        object Deleted : EventState()
    }

    private fun getToken(): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }

    fun getEvents() {
        viewModelScope.launch {
            eventState = EventState.Loading
            val token = getToken() ?: run {
                eventState = EventState.Error("Не авторизован")
                return@launch
            }
            try {
                val response = eventApi.getEvents("Bearer $token")
                _events.value = response
                eventState = EventState.Idle
            } catch (e: Exception) {
                eventState = EventState.Error(e.message ?: "Не удалось загрузить события")
            }
        }
    }

    fun createEvent(title: String, description: String?, date: Date, location: String, city: String, status: EventStatus = EventStatus.DRAFT) {
        viewModelScope.launch {
            eventState = EventState.Loading
            val token = getToken() ?: run {
                eventState = EventState.Error("Не авторизован")
                return@launch
            }
            try {
                val response = eventApi.createEvent(
                    EventRequest(title, description, date, location, city, status),
                    "Bearer $token"
                )
                _events.value = _events.value + response
                eventState = EventState.Success(response)
                kotlinx.coroutines.delay(100)
                eventState = EventState.Idle
            } catch (e: Exception) {
                eventState = EventState.Error(e.message ?: "Не удалось создать событие")
            }
        }
    }

    fun updateEvent(id: Int, title: String, description: String?, date: Date, location: String, city: String, status: EventStatus) {
        viewModelScope.launch {
            eventState = EventState.Loading
            val token = getToken() ?: run {
                eventState = EventState.Error("Не авторизован")
                return@launch
            }
            try {
                val response = eventApi.updateEvent(
                    id,
                    EventRequest(title, description, date, location, city, status),
                    "Bearer $token"
                )
                _events.value = _events.value.map { if (it.id == id) response else it }
                eventState = EventState.Success(response)
                kotlinx.coroutines.delay(100)
                eventState = EventState.Idle
            } catch (e: Exception) {
                eventState = EventState.Error(e.message ?: "Не удалось обновить событие")
            }
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            eventState = EventState.Loading
            val token = getToken() ?: run {
                eventState = EventState.Error("Не авторизован")
                return@launch
            }
            try {
                eventApi.deleteEvent(id, "Bearer $token")
                _events.value = _events.value.filter { it.id != id }
                eventState = EventState.Deleted
                kotlinx.coroutines.delay(100)
                eventState = EventState.Idle
            } catch (e: Exception) {
                eventState = EventState.Error(e.message ?: "Не удалось удалить событие")
            }
        }
    }

    fun searchEvents(query: String) {
        viewModelScope.launch {
            eventState = EventState.Loading
            val token = getToken() ?: run {
                eventState = EventState.Error("Не авторизован")
                return@launch
            }
            try {
                val response = eventApi.searchEvents("Bearer $token", query)
                _searchResults.value = response
                eventState = EventState.Idle
            } catch (e: Exception) {
                eventState = EventState.Error(e.message ?: "Не удалось выполнить поиск")
            }
        }
    }
}