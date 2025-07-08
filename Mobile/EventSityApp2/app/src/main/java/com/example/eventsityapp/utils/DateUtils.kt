package com.example.eventsityapp.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object DateUtils {
    fun Date.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
    }

    fun LocalDateTime.toDate(): Date {
        return Date.from(atZone(ZoneId.systemDefault()).toInstant())
    }
}