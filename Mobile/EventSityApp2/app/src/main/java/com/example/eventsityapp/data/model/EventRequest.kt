package com.example.eventsityapp.data.model

import com.example.eventsityapp.data.model.EventStatus
import java.util.Date

data class EventRequest(
    val title: String,
    val description: String?,
    val date: Date,
    val location: String,
    val city: String,
    val status: EventStatus = EventStatus.DRAFT
)