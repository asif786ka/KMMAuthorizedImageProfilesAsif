package com.asif.kmmauthorizedimageprofiles

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ApiService {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient()

    private val baseUrl = "https://authorizedimageprofilesasif-fb449ed181c9.herokuapp.com"
    private val logger = Logger.withTag("ApiService")

    suspend fun register(email: String, password: String): TokenResponse? {
        return try {
            val requestBody = json.encodeToString(RegisterRequest(email, password))
            val response: HttpResponse = client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            handleResponse<TokenResponse>(response)
        } catch (e: Exception) {
            logger.e(e) { "Error during registration" }
            null
        }
    }

    suspend fun login(email: String, password: String): TokenResponse? {
        return try {
            val requestBody = json.encodeToString(LoginRequest(email, password))
            val response: HttpResponse = client.post("$baseUrl/sessions/new") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            handleResponse<TokenResponse>(response)
        } catch (e: Exception) {
            logger.e(e) { "Error during login" }
            null
        }
    }

    suspend fun getProfile(userId: String, token: String): ProfileResponse? {
        return try {
            val response: HttpResponse = client.get("$baseUrl/users/$userId") {
                header("Authorization", "Bearer $token")
            }
            handleResponse<ProfileResponse>(response)
        } catch (e: Exception) {
            logger.e(e) { "Error fetching profile" }
            null
        }
    }

    suspend fun updateAvatar(userId: String, avatar: String, token: String): AvatarResponse? {
        return try {
            val requestBody = json.encodeToString(AvatarRequest(avatar))
            val response: HttpResponse = client.post("$baseUrl/users/$userId/avatar") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            handleResponse<AvatarResponse>(response)
        } catch (e: Exception) {
            logger.e(e) { "Error updating avatar" }
            null
        }
    }

    // Generic function to handle responses and errors
    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T? {
        val rawResponse = response.bodyAsText()
        logger.d { "Raw response: $rawResponse" }

        return if (response.status.value != 401 || response.status.value != 404) {
            try {
                val result = json.decodeFromString<T>(rawResponse)
                logger.d { "Parsed response: $result" }
                result
            } catch (e: Exception) {
                logger.e(e) { "Error decoding response" }
                null
            }
        } else {
            try {
                val error = json.decodeFromString<ErrorResponse>(rawResponse)
                logger.e { "API error: ${error.error}" }
                null
            } catch (e: Exception) {
                logger.e(e) { "Error decoding error response" }
                null
            }
        }
    }
}
