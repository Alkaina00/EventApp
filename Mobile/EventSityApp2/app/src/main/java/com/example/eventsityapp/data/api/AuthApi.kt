package com.example.eventsityapp.data.api

import android.util.Log
import com.example.eventsityapp.data.model.EventResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val userId: String)
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val profilePhoto: String? // Добавляем поле для фотографии профиля
)
data class UpdateProfileRequest(val name: String, val phone: String?)

interface AuthApi {
    @POST("/api/users/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/api/users/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("/api/users/profile")
    suspend fun getProfile(@Header("Authorization") token: String): UserProfile

    @POST("/api/users/logout")
    suspend fun logout(@Header("Authorization") token: String)

    @Multipart
    @PUT("/api/users/profile")
    suspend fun updateProfileWithPhoto(
        @Header("Authorization") token: String,
        @Part("name") name: String,
        @Part("phone") phone: String?,
        @Part photo: MultipartBody.Part?
    ): UserProfile
}

interface EventApi {
    @GET("/api/events")
    suspend fun getEvents(@Header("Authorization") token: String): List<EventResponse>

    @POST("/api/events")
    suspend fun createEvent(@Body request: EventRequest, @Header("Authorization") token: String): EventResponse

    @PUT("/api/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body request: EventRequest, @Header("Authorization") token: String): EventResponse

    @DELETE("/api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int, @Header("Authorization") token: String)
}

data class EventRequest(
    val title: String,
    val description: String?,
    val date: Date,
    val location: String,
    val city: String
)

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3001/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(json.asJsonPrimitive.asString)
            } catch (e: Exception) {
                Log.e("RetrofitClient", "Failed to parse date: ${json.asJsonPrimitive.asString}", e)
                null
            }
        })
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val eventApi: EventApi by lazy {
        retrofit.create(EventApi::class.java)
    }
}