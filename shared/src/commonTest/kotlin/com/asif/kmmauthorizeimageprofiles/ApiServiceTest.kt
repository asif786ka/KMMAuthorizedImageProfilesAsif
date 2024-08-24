package com.asif.kmmauthorizeimageprofiles

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.asif.kmmauthorizedimageprofiles.ApiService
import com.asif.kmmauthorizedimageprofiles.ProfileResponse
import com.asif.kmmauthorizedimageprofiles.TokenResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestLogger : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        println("[$severity] $tag: $message")
        throwable?.printStackTrace()
    }
}
class ApiServiceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @BeforeTest
    fun setupLogger() {
        Logger.setLogWriters(TestLogger())
    }
    // Helper function to create a mock client
    private fun createMockClient(responseHandler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler(responseHandler)
            }
        }
    }

    @Test
    fun `register success returns TokenResponse`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/register" -> {
                    respond(
                        content = json.encodeToString(TokenResponse("fake-userid", "fake-token")),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.register("test@example.com", "password123")
        assertEquals("fake-token", response?.token)
        assertEquals("fake-userid", response?.userid)
    }

    @Test
    fun `register failure returns null`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/register" -> respond(
                    content = "Bad Request",
                    status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.register("test@example.com", "password123")
        assertNull(response)
    }

    @Test
    fun `login success returns TokenResponse`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/sessions/new" -> {
                    respond(
                        content = json.encodeToString(TokenResponse("fake-userid", "fake-token")),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.login("test@example.com", "password123")
        assertEquals("fake-token", response?.token)
        assertEquals("fake-userid", response?.userid)
    }

    @Test
    fun `login failure returns null`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/sessions/new" -> respond(
                    content = "Unauthorized",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.login("test@example.com", "password123")
        assertNull(response)
    }

    @Test
    fun `getProfile success returns ProfileResponse`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/users/1" -> {
                    respond(
                        content = json.encodeToString(ProfileResponse("1", "test@example.com")),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.getProfile("1", "fake-token")
        assertEquals("1", response?.email)
        assertEquals("test@example.com", response?.avatar_url)
    }

    @Test
    fun `getProfile failure returns null`() = runBlocking {
        val mockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/users/1" -> respond(
                    content = "Not Found",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
                else -> error("Unhandled request: ${request.url}")
            }
        }

        val apiService = ApiService(mockClient)
        val response = apiService.getProfile("1", "fake-token")
        assertNull(response)
    }
}
