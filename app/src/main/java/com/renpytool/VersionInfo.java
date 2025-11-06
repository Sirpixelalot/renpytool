package com.renpytool;

/**
 * Data class to hold version information from GitHub releases
 */
public class VersionInfo {
    private final String versionTag;
    private final String downloadUrl;
    private final String releaseNotes;
    private final String publishedAt;

    public VersionInfo(String versionTag, String downloadUrl, String releaseNotes, String publishedAt) {
        this.versionTag = versionTag;
        this.downloadUrl = downloadUrl;
        this.releaseNotes = releaseNotes;
        this.publishedAt = publishedAt;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * Extract version number from tag (e.g., "v1.0" -> "1.0", "1.0.5" -> "1.0.5")
     */
    public String getVersionNumber() {
        if (versionTag == null) return "";
        return versionTag.startsWith("v") ? versionTag.substring(1) : versionTag;
    }

    /**
     * Compare this version with another version string
     * Returns: 1 if this version is newer, -1 if older, 0 if equal
     */
    public int compareToVersion(String otherVersion) {
        String thisVersion = getVersionNumber();
        String[] thisParts = thisVersion.split("\\.");
        String[] otherParts = otherVersion.split("\\.");

        int maxLength = Math.max(thisParts.length, otherParts.length);

        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < thisParts.length ? parseVersionPart(thisParts[i]) : 0;
            int otherPart = i < otherParts.length ? parseVersionPart(otherParts[i]) : 0;

            if (thisPart > otherPart) return 1;
            if (thisPart < otherPart) return -1;
        }

        return 0;  // versions are equal
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isNewerThan(String currentVersion) {
        return compareToVersion(currentVersion) > 0;
    }
}
