
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

    private var glyphManager: GlyphDeliveryManager? = null
    private var matrixManager: GlyphMatrixDeliveryManager? = null
    private val handler = Handler(Looper.getMainLooper())

    // key="pkg:notifId" → handler runnable (AT_DOOR zamanlayıcı)
    private val resetRunnables = mutableMapOf<String, Runnable>()

    override fun onCreate() {
        super.onCreate()
        AppSettings.init(applicationContext)

        glyphManager = GlyphDeliveryManager(applicationContext).apply { init() }

        matrixManager = GlyphMatrixDeliveryManager(applicationContext).apply {
            if (isSupported()) init()
        }

        // OrderTracker değişikliklerini Glyph ve Matrix'e yansıt
        OrderTracker.onStatusChanged = { newStatus ->
            val speed = AppSettings.getBlinkSpeed()

            val gm = glyphManager
            if (gm?.isReady() == true) {
                gm.applyStatus(newStatus, speed)
            } else {
                Log.w(TAG, "Glyph hazır değil, durum atlanıyor: $newStatus")
            }

            val mm = matrixManager
            if (mm?.isSupported() == true && mm.isReady()) {
                mm.applyStatus(newStatus, speed)
            }
        }

        Log.d(TAG, "DeliveryNotificationListener başlatıldı")
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        synchronized(resetRunnables) {
            resetRunnables.clear()
        }
        OrderTracker.onStatusChanged = null
        OrderTracker.clearAll()
        glyphManager?.destroy()
        glyphManager = null
        matrixManager?.destroy()
        matrixManager = null
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        // Desteklenen uygulama kontrolü (built-in + özel)
        if (!isEnabledPackage(pkg)) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val text  = extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d(TAG, "[$pkg] title=\"$title\" | text=\"$text\"")

        val status = DeliveryParser.parse(title, text) ?: return
        Log.d(TAG, "Tespit: $status")

        val orderKey = "$pkg:${sbn.id}"
        
        // Önceki zamanlayıcıyı iptal et (aynı sipariş için yeni güncelleme)
        cancelScheduledRemoval(orderKey)
        
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
                notificationTitle = title,
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
        
        // Hemen temizleme yapma, kısa bir gecikme ile
        // (aynı sipariş için hemen yeni bildirim gelebilir)
        scheduleOrderRemoval(orderKey, 3_000L)
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
            synchronized(resetRunnables) {
                // Sadece bu runnable hâlâ map'te ise çalıştır (güvenlik için)
                if (resetRunnables.remove(key) != null) {
                    OrderTracker.remove(key)
                    if (OrderTracker.highestStatus() == DeliveryStatus.IDLE) {
                        glyphManager?.reset()
                        matrixManager?.reset()
                    }
                }
            }
        }
        
        synchronized(resetRunnables) {
            resetRunnables[key] = runnable
        }
        handler.postDelayed(runnable, delayMs)
    }

    private fun cancelScheduledRemoval(key: String) {
        synchronized(resetRunnables) {
            resetRunnables.remove(key)?.let { handler.removeCallbacks(it) }
        }
    }

    companion object {
        private const val TAG = "DeliveryGlyphListener"
    }
}
