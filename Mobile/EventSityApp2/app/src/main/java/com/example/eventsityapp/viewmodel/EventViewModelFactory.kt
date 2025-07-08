package com.example.eventsityapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventsityapp.data.api.RetrofitClient

class EventViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(RetrofitClient.eventApi, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}