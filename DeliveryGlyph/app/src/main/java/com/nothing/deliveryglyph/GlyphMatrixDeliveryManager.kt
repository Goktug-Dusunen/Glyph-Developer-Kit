/*
 * Delivery Glyph — Developer: Gdusunen
 *
 * Glyph Matrix desteği:
 *   CMF Phone (1)    23112 — Matrix ekranı
 *   Phone (4a) Pro   25111p — Matrix ekranı
 *
 * Matrix API: GlyphMatrixManager + GlyphMatrixFrame + GlyphMatrixObject
 */
package com.nothing.deliveryglyph

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject

class GlyphMatrixDeliveryManager(private val context: Context) {

    private var mGMM: GlyphMatrixManager? = null
    @Volatile private var isConnected = false
    private var currentStatus = DeliveryStatus.IDLE

    private val callback = object : GlyphMatrixManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            try {
                val device = when {
                    Common.is23112()  -> Glyph.DEVICE_23112
                    Common.is25111p() -> Glyph.DEVICE_25111p
                    else -> {
                        Log.e(TAG, "Matrix desteklenen cihaz bulunamadı")
                        return
                    }
                }
                mGMM?.register(device)
                mGMM?.setGlyphMatrixTimeout(false)
                isConnected = true
                Log.d(TAG, "GlyphMatrix session başladı (device=$device)")
            } catch (e: Exception) {
                Log.e(TAG, "GlyphMatrix bağlantı hatası: ${e.message}")
                isConnected = false
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            isConnected = false
            Log.d(TAG, "GlyphMatrix servisi bağlantısı koptu")
        }
    }

    fun init() {
        if (!isSupported()) return
        if (mGMM != null) return
        mGMM = GlyphMatrixManager.getInstance(context)
        mGMM?.init(callback)
    }

    fun destroy() {
        if (!isSupported() || mGMM == null) return
        try {
            mGMM?.turnOff()
            mGMM?.closeAppMatrix()
        } catch (e: Exception) {
            Log.e(TAG, "Matrix kapatma hatası: ${e.message}")
        }
        mGMM?.unInit()
        mGMM = null
        isConnected = false
    }

    @Synchronized
    fun applyStatus(status: DeliveryStatus, blinkSpeed: BlinkSpeed = BlinkSpeed.NORMAL) {
        if (!isConnected) return
        if (status == currentStatus) return
        currentStatus = status

        try {
            mGMM?.turnOff()
            when (status) {
                DeliveryStatus.IDLE    -> { /* kapalı */ }
                DeliveryStatus.AT_DOOR -> showAtDoor()
                else                   -> showStatusText(status)
            }
            Log.d(TAG, "GlyphMatrix → $status")
        } catch (e: Exception) {
            Log.e(TAG, "GlyphMatrix hatası: ${e.message}")
        }
    }

    fun reset() {
        currentStatus = DeliveryStatus.IDLE
        if (!isConnected) return
        try {
            mGMM?.turnOff()
        } catch (e: Exception) {
            Log.e(TAG, "Matrix reset hatası: ${e.message}")
        }
    }

    fun isSupported(): Boolean = Common.is23112() || Common.is25111p()
    fun isReady(): Boolean = isConnected

    // ─── Durum metinleri ─────────────────────────────────────────────────────

    private fun showStatusText(status: DeliveryStatus) {
        val text = when (status) {
            DeliveryStatus.CONFIRMED  -> "ONAY"
            DeliveryStatus.PREPARING  -> "HAZR"
            DeliveryStatus.ON_THE_WAY -> "YOLDA"
            else                      -> return
        }
        displayText(text)
    }

    // ─── Kapıda animasyonu ────────────────────────────────────────────────────

    private fun showAtDoor() {
        displayText("KAPI!")
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private fun displayText(text: String) {
        val obj = GlyphMatrixObject.Builder()
            .setText(text)
            .build()
        val frame = GlyphMatrixFrame.Builder()
            .addTop(obj)
            .build(context)
        mGMM?.setAppMatrixFrame(frame)
    }

    companion object {
        private const val TAG = "GlyphMatrixDelivery"
    }
}
