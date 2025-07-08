// Event.kt
package com.example.eventsityapp.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

enum class EventStatus {
    DRAFT,      // Проект
    PUBLISHED,  // Размещен
    CANCELLED,  // Отменен
    COMPLETED   // Прошел
}

data class EventResponse(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("event_date") val event_date: Date,
    val location: String,
    val city: String,
    val creator_id: Int,
    val status: EventStatus,
    @SerializedName("created_at") val created_at: Date? = null
)