package com.example.eventsityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.eventsityapp.data.api.RetrofitClient
import com.example.eventsityapp.viewmodel.AuthViewModel
import com.example.eventsityapp.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventViewModel = EventViewModel(RetrofitClient.eventApi, applicationContext)
        val authViewModel = AuthViewModel(RetrofitClient.authApi, applicationContext)

        setContent {
            AppNavigation(eventViewModel, authViewModel)
        }
    }
}