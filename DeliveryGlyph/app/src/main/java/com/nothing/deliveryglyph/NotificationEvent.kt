/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

data class NotificationEvent(
    val timestampMs: Long,
    val packageName: String,
    val appLabel: String,
    val notificationTitle: String,
    val statusLabel: String
) {
    fun serialize(): String =
        "$timestampMs\u0001$packageName\u0001$appLabel\u0001$notificationTitle\u0001$statusLabel"

    companion object {
        fun deserialize(raw: String): NotificationEvent? {
            val parts = raw.split("\u0001", limit = 5)
            if (parts.size < 5) return null
            return runCatching {
                NotificationEvent(
                    timestampMs       = parts[0].toLong(),
                    packageName       = parts[1],
                    appLabel          = parts[2],
                    notificationTitle = parts[3],
                    statusLabel       = parts[4]
                )
            }.getOrNull()
        }
    }
}
