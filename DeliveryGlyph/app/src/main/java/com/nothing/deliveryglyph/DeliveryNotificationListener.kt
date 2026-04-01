/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class DeliveryNotificationListener : NotificationListenerService() {

    private lateinit var glyphManager: GlyphDeliveryManager
    private val handler = Handler(Looper.getMainLooper())

    // key="pkg:notifId" → handler runnable (AT_DOOR zamanlayıcı)
    private val resetRunnables = mutableMapOf<String, Runnable>()

    override fun onCreate() {
        super.onCreate()
        AppSettings.init(applicationContext)

        glyphManager = GlyphDeliveryManager(applicationContext)
        glyphManager.init()

        // OrderTracker değişikliklerini Glyph'e yansıt
        OrderTracker.onStatusChanged = { newStatus ->
            val speed = AppSettings.getBlinkSpeed()
            glyphManager.applyStatus(newStatus, speed)
        }

        Log.d(TAG, "DeliveryNotificationListener başlatıldı")
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        OrderTracker.onStatusChanged = null
        OrderTracker.clearAll()
        glyphManager.destroy()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        // Desteklenen uygulama kontrolü (built-in + özel)
        if (!isEnabledPackage(pkg)) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title")
        val text  = extras.getCharSequence("android.text")?.toString()

        Log.d(TAG, "[$pkg] title=\"$title\" | text=\"$text\"")

        val status = DeliveryParser.parse(title, text) ?: return
        Log.d(TAG, "Tespit: $status")

        val orderKey = "$pkg:${sbn.id}"
        OrderTracker.update(orderKey, status)

        // Geçmişe yaz
        val appLabel = DeliveryParser.ALL_APPS.firstOrNull { it.packageName == pkg }?.displayName
            ?: AppSettings.getCustomApps().firstOrNull { it.packageName == pkg }?.displayName
            ?: pkg
        AppSettings.appendHistory(
            NotificationEvent(
                timestampMs       = System.currentTimeMillis(),
                packageName       = pkg,
                appLabel          = appLabel,
                notificationTitle = title ?: "",
                statusLabel       = status.label
            )
        )

        // AT_DOOR → 30 sn sonra bu siparişi temizle
        if (status == DeliveryStatus.AT_DOOR) {
            scheduleOrderRemoval(orderKey, 30_000L)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (!isEnabledPackage(pkg)) return

        val orderKey = "$pkg:${sbn.id}"
        cancelScheduledRemoval(orderKey)

        // 5 sn gecikme ile temizle (aynı sipariş için yeni bildirim gelebilir)
        scheduleOrderRemoval(orderKey, 5_000L)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun isEnabledPackage(pkg: String): Boolean {
        val isBuiltIn = DeliveryParser.isBuiltInApp(pkg) && AppSettings.isAppEnabled(pkg)
        val isCustom  = AppSettings.getCustomApps().any { it.packageName == pkg && it.isEnabled }
        return isBuiltIn || isCustom
    }

    private fun scheduleOrderRemoval(key: String, delayMs: Long) {
        cancelScheduledRemoval(key)
        val runnable = Runnable {
            resetRunnables.remove(key)
            OrderTracker.remove(key)
            if (OrderTracker.highestStatus() == DeliveryStatus.IDLE) {
                glyphManager.reset()
            }
        }
        resetRunnables[key] = runnable
        handler.postDelayed(runnable, delayMs)
    }

    private fun cancelScheduledRemoval(key: String) {
        resetRunnables.remove(key)?.let { handler.removeCallbacks(it) }
    }

    companion object {
        private const val TAG = "DeliveryGlyphListener"
    }
}
