package com.renpytool

/**
 * Data class to hold version information from GitHub releases
 */
data class VersionInfo(
    val versionTag: String,
    val downloadUrl: String,
    val releaseNotes: String = "",
    val publishedAt: String = ""
) {
    /**
     * Extract version number from tag (e.g., "v1.0" -> "1.0", "1.0.5" -> "1.0.5")
     */
    val versionNumber: String
        get() = if (versionTag.startsWith("v", ignoreCase = true)) {
            versionTag.substring(1)
        } else {
            versionTag
        }

    /**
     * Compare this version with another version string
     * Returns: 1 if this version is newer, -1 if older, 0 if equal
     */
    fun compareToVersion(otherVersion: String): Int {
        val thisParts = versionNumber.split(".")
        val otherParts = otherVersion.split(".")
        val maxLength = maxOf(thisParts.size, otherParts.size)

        for (i in 0 until maxLength) {
            val thisPart = thisParts.getOrNull(i)?.toIntOrNull() ?: 0
            val otherPart = otherParts.getOrNull(i)?.toIntOrNull() ?: 0

            when {
                thisPart > otherPart -> return 1
                thisPart < otherPart -> return -1
            }
        }

        return 0  // versions are equal
    }

    /**
     * Check if this version is newer than the given version
     */
    fun isNewerThan(currentVersion: String): Boolean = compareToVersion(currentVersion) > 0
}
