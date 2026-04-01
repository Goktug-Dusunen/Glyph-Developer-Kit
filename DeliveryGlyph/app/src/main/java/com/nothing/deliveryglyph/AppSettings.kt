/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton for all SharedPreferences operations.
 * Call [init] once in each Android component before using any method.
 */
object AppSettings {

    private const val PREFS_NAME      = "delivery_glyph_prefs"
    private const val KEY_BLINK_SPEED = "blink_speed"
    private const val KEY_CUSTOM_APPS = "custom_apps"
    private const val KEY_HISTORY     = "history"
    private const val HISTORY_MAX     = 10
    private const val RECORD_SEP      = "\u0002"   // between records
    private const val FIELD_SEP       = "\u0003"   // between custom-app fields

    private var _prefs: SharedPreferences? = null
    private val prefs: SharedPreferences
        get() = _prefs ?: error("AppSettings.init(context) must be called first")

    fun init(context: Context) {
        if (_prefs == null) {
            _prefs = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ── App enabled / disabled ────────────────────────────────────────────────

    fun isAppEnabled(packageName: String): Boolean =
        prefs.getBoolean("enabled_$packageName", true)

    fun setAppEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("enabled_$packageName", enabled).apply()
    }

    // ── Blink speed ──────────────────────────────────────────────────────────

    fun getBlinkSpeed(): BlinkSpeed =
        BlinkSpeed.fromName(prefs.getString(KEY_BLINK_SPEED, BlinkSpeed.NORMAL.name) ?: BlinkSpeed.NORMAL.name)

    fun setBlinkSpeed(speed: BlinkSpeed) {
        prefs.edit().putString(KEY_BLINK_SPEED, speed.name).apply()
    }

    // ── Custom apps ───────────────────────────────────────────────────────────

    fun getCustomApps(): List<AppEntry> {
        val raw = prefs.getString(KEY_CUSTOM_APPS, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { record ->
            val parts = record.split(FIELD_SEP)
            if (parts.size < 3) return@mapNotNull null
            AppEntry(
                packageName = parts[0],
                displayName = parts[1],
                isBuiltIn   = false,
                isEnabled   = parts[2] == "1"
            )
        }
    }

    fun setCustomApps(list: List<AppEntry>) {
        val raw = list.joinToString(RECORD_SEP) { app ->
            "${app.packageName}$FIELD_SEP${app.displayName}$FIELD_SEP${if (app.isEnabled) "1" else "0"}"
        }
        prefs.edit().putString(KEY_CUSTOM_APPS, raw).apply()
    }

    fun addCustomApp(packageName: String, displayName: String) {
        val current = getCustomApps().toMutableList()
        if (current.none { it.packageName == packageName }) {
            current.add(AppEntry(packageName, displayName, isBuiltIn = false, isEnabled = true))
            setCustomApps(current)
        }
    }

    fun removeCustomApp(packageName: String) {
        val filtered = getCustomApps().filter { it.packageName != packageName }
        setCustomApps(filtered)
    }

    fun setCustomAppEnabled(packageName: String, enabled: Boolean) {
        val updated = getCustomApps().map {
            if (it.packageName == packageName) it.copy(isEnabled = enabled) else it
        }
        setCustomApps(updated)
    }

    // ── Notification history ─────────────────────────────────────────────────

    fun appendHistory(event: NotificationEvent) {
        val existing = getHistory().toMutableList()
        existing.add(0, event)                      // newest first
        val trimmed = existing.take(HISTORY_MAX)
        val raw = trimmed.joinToString(RECORD_SEP) { it.serialize() }
        prefs.edit().putString(KEY_HISTORY, raw).apply()
    }

    fun getHistory(): List<NotificationEvent> {
        val raw = prefs.getString(KEY_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEP).mapNotNull { NotificationEvent.deserialize(it) }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
