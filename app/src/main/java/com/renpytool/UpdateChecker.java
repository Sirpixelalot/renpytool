package com.renpytool;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks for updates from GitHub releases
 */
public class UpdateChecker {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Sirpixelalot/renpytool/releases/latest";
    private static final int TIMEOUT_MS = 10000;  // 10 seconds

    public interface UpdateCheckCallback {
        void onUpdateAvailable(VersionInfo versionInfo);
        void onNoUpdateAvailable();
        void onCheckFailed(String error);
    }

    /**
     * Check for updates on GitHub releases
     * This method should be called from a background thread
     */
    public static void checkForUpdates(String currentVersion, UpdateCheckCallback callback) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // Create connection
            URL url = new URL(GITHUB_API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                callback.onCheckFailed("HTTP error: " + responseCode);
                return;
            }

            // Read response
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            // Extract version information
            String tagName = jsonResponse.has("tag_name") ? jsonResponse.get("tag_name").getAsString() : null;
            String body = jsonResponse.has("body") ? jsonResponse.get("body").getAsString() : "";
            String publishedAt = jsonResponse.has("published_at") ? jsonResponse.get("published_at").getAsString() : "";

            // Get download URL from assets (if available)
            String downloadUrl = jsonResponse.has("html_url") ? jsonResponse.get("html_url").getAsString() : "";

            // Check if tag name is valid
            if (tagName == null || tagName.isEmpty()) {
                callback.onCheckFailed("Invalid response from GitHub");
                return;
            }

            // Create version info object
            VersionInfo versionInfo = new VersionInfo(tagName, downloadUrl, body, publishedAt);

            // Compare versions
            if (versionInfo.isNewerThan(currentVersion)) {
                callback.onUpdateAvailable(versionInfo);
            } else {
                callback.onNoUpdateAvailable();
            }

        } catch (Exception e) {
            // Silent fail - don't bother user with network errors
            callback.onCheckFailed("Network error: " + e.getMessage());

        } finally {
            // Clean up resources
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                // Ignore
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
