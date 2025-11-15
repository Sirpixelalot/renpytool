package com.renpytool

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks for updates from GitHub releases using Kotlin coroutines
 */
object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Sirpixelalot/Rentool/releases/latest"
    private const val TIMEOUT_MS = 10000  // 10 seconds

    sealed class UpdateResult {
        data class UpdateAvailable(val versionInfo: VersionInfo) : UpdateResult()
        object NoUpdateAvailable : UpdateResult()
        data class CheckFailed(val error: String) : UpdateResult()
    }

    /**
     * Check for updates on GitHub releases
     * This is a suspend function that runs on IO dispatcher
     */
    suspend fun checkForUpdates(currentVersion: String): UpdateResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Checking for updates... Current version: $currentVersion")
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            // Create connection
            val url = URL(GITHUB_API_URL)
            Log.d(TAG, "Connecting to: $GITHUB_API_URL")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            // Check response code
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error: $responseCode")
                return@withContext UpdateResult.CheckFailed("HTTP error: $responseCode")
            }

            // Read response
            reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()

            // Parse JSON response
            val jsonResponse = JsonParser.parseString(response).asJsonObject

            // Extract version information
            val tagName = jsonResponse.get("tag_name")?.asString
            val body = jsonResponse.get("body")?.asString ?: ""
            val publishedAt = jsonResponse.get("published_at")?.asString ?: ""
            val downloadUrl = jsonResponse.get("html_url")?.asString ?: ""

            Log.d(TAG, "Latest version from GitHub: $tagName")
            Log.d(TAG, "Download URL: $downloadUrl")

            // Check if tag name is valid
            if (tagName.isNullOrEmpty()) {
                Log.e(TAG, "Invalid tag name from GitHub")
                return@withContext UpdateResult.CheckFailed("Invalid response from GitHub")
            }

            // Create version info object
            val versionInfo = VersionInfo(tagName, downloadUrl, body, publishedAt)

            // Compare versions
            Log.d(TAG, "Comparing: ${versionInfo.versionNumber} vs $currentVersion")
            if (versionInfo.isNewerThan(currentVersion)) {
                Log.d(TAG, "Update available!")
                UpdateResult.UpdateAvailable(versionInfo)
            } else {
                Log.d(TAG, "No update available")
                UpdateResult.NoUpdateAvailable
            }

        } catch (e: Exception) {
            // Silent fail - don't bother user with network errors
            Log.e(TAG, "Update check failed", e)
            UpdateResult.CheckFailed("Network error: ${e.message}")

        } finally {
            // Clean up resources
            reader?.close()
            connection?.disconnect()
        }
    }

    /**
     * Legacy callback interface for backward compatibility
     */
    interface UpdateCheckCallback {
        fun onUpdateAvailable(versionInfo: VersionInfo)
        fun onNoUpdateAvailable()
        fun onCheckFailed(error: String)
    }

    /**
     * Legacy method with callback for backward compatibility
     */
    @Suppress("unused")
    suspend fun checkForUpdates(currentVersion: String, callback: UpdateCheckCallback) {
        when (val result = checkForUpdates(currentVersion)) {
            is UpdateResult.UpdateAvailable -> callback.onUpdateAvailable(result.versionInfo)
            is UpdateResult.NoUpdateAvailable -> callback.onNoUpdateAvailable()
            is UpdateResult.CheckFailed -> callback.onCheckFailed(result.error)
        }
    }
}
