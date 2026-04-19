
/*
 * Delivery Glyph — Developer: Gdusunen
 *
 * Model desteği:
 *   Phone (1)       20111 — A,B,C,D,E  → progress: D kanalı
 *   Phone (2)       22111 — A,B,C,D,E  → progress: C kanalı
 *   Phone (2a)      23111 — A,B,C      → progress: C kanalı
 *   Phone (2a) Plus 23113 — A,B,C      → progress: C kanalı
 *   Phone (3a/Pro)  24111 — A,B,C      → progress: C kanalı
 *   Phone (4a)      25111 — A (6 LED)  → manuel LED kontrolü
 *   CMF Phone (1)   23112 — A,B,C      → Matrix + C kanalı (Glyph)
 *   Phone (4a) Pro  25111p — A (6 LED) → Matrix + manuel LED (Glyph)
 */
package com.nothing.deliveryglyph

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager

class GlyphDeliveryManager(private val context: Context) {

    private var mGM: GlyphManager? = null
    @Volatile
    private var isSessionOpen = false
    @Volatile
    private var isRegistered = false
    private var currentStatus = DeliveryStatus.IDLE

    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            try {
                val device = when {
                    Common.is20111()  -> Glyph.DEVICE_20111
                    Common.is22111()  -> Glyph.DEVICE_22111
                    Common.is23111()  -> Glyph.DEVICE_23111
                    Common.is23112()  -> Glyph.DEVICE_23112
                    Common.is23113()  -> Glyph.DEVICE_23113
                    Common.is24111()  -> Glyph.DEVICE_24111
                    Common.is25111()  -> Glyph.DEVICE_25111
                    Common.is25111p() -> Glyph.DEVICE_25111p
                    else -> {
                        Log.e(TAG, "Desteklenmeyen cihaz, Glyph SDK kullanılamıyor")
                        return
                    }
                }
                
                mGM?.register(device)
                isRegistered = true
                mGM?.openSession()
                isSessionOpen = true
                Log.d(TAG, "Glyph session açıldı (device=$device)")
            } catch (e: GlyphException) {
                Log.e(TAG, "Session açılamadı: ${e.message}")
                isSessionOpen = false
                isRegistered = false
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            // Servis zaten disconnect oldu, closeSession() çağırmaya gerek yok
            isSessionOpen = false
            isRegistered = false
            Log.d(TAG, "Glyph servisi bağlantısı koptu")
        }
    }

    fun init() {
        if (mGM != null) {
            Log.w(TAG, "GlyphDeliveryManager zaten init edilmiş")
            return
        }
        mGM = GlyphManager.getInstance(context)
        mGM?.init(callback)
    }

    fun destroy() {
        if (!isSessionOpen) {
            mGM?.unInit()
            mGM = null
            return
        }
        
        try {
            mGM?.turnOff()
            mGM?.closeSession()
        } catch (e: GlyphException) {
            Log.e(TAG, "Session kapatma hatası: ${e.message}")
        }
        mGM?.unInit()
        mGM = null
        isSessionOpen = false
        isRegistered = false
    }

    /**
     * @param status Yeni teslimat aşaması
     * @param blinkSpeed AT_DOOR animasyonu için hız ayarı
     */
    @Synchronized
    fun applyStatus(status: DeliveryStatus, blinkSpeed: BlinkSpeed = BlinkSpeed.NORMAL) {
        if (!isSessionOpen || !isRegistered) {
            Log.w(TAG, "applyStatus çağrıldı ama session açık değil")
            return
        }
        if (status == currentStatus) return
        currentStatus = status

        try {
            mGM?.turnOff()
            when (status) {
                DeliveryStatus.IDLE    -> { /* kapalı */ }
                DeliveryStatus.AT_DOOR -> blinkAll(blinkSpeed)
                else                   -> showProgress(status.progress)
            }
            Log.d(TAG, "Glyph → $status  (blinkSpeed=$blinkSpeed)")
        } catch (e: GlyphException) {
            Log.e(TAG, "Glyph hatası: ${e.message}")
        }
    }

    fun reset() {
        currentStatus = DeliveryStatus.IDLE
        if (!isSessionOpen) return
        
        try {
            mGM?.turnOff()
        } catch (e: GlyphException) {
            Log.e(TAG, "Reset hatası: ${e.message}")
        }
    }

    fun isReady(): Boolean = isSessionOpen && isRegistered

    // ─── Progress gösterimi ───────────────────────────────────────────────────

    private fun showProgress(progress: Int) {
        when {
            Common.is25111() || Common.is25111p() -> showProgressPhone4a(progress)
            Common.is20111()                      -> showProgressWithChannel(progress, useD = true)
            else                                  -> showProgressWithChannel(progress, useD = false)
        }
    }

    /**
     * Phone (1)       → D kanalı (D1_1–D1_8)
     * Phone (2/2a/3a) → C kanalı
     */
    private fun showProgressWithChannel(progress: Int, useD: Boolean) {
        val builder = mGM?.getGlyphFrameBuilder() ?: return
        val frame: GlyphFrame = if (useD) {
            builder.buildChannelD().buildPeriod(0).build()
        } else {
            builder.buildChannelC().buildPeriod(0).build()
        }
        mGM?.displayProgress(frame, progress.coerceIn(0, 100))
    }

    /**
     * Phone (4a) — Sadece A1-A6 (index 0-5).
     * displayProgress C/D kanalı beklediği için manuel LED kontrolü.
     *
     * A1=top=index0, A6=bottom=index5 — alttan üste doldurur:
     *   0-24%  → [5]          = 1 LED (A6)
     *   25-49% → [3,4,5]      = 3 LED (A4,A5,A6)
     *   50-74% → [1,2,3,4,5]  = 5 LED (A2-A6)
     *   75-100%→ [0,1,2,3,4,5]= 6 LED (A1-A6, tamamı)
     */
    private fun showProgressPhone4a(progress: Int) {
        val builder = mGM?.getGlyphFrameBuilder() ?: return
        val normalizedProgress = progress.coerceIn(0, 100)
        
        val indices: IntArray = when {
            normalizedProgress >= 75 -> intArrayOf(0, 1, 2, 3, 4, 5)  // %75-100: Tümü
            normalizedProgress >= 50 -> intArrayOf(1, 2, 3, 4, 5)      // %50-74: 5 LED
            normalizedProgress >= 25 -> intArrayOf(3, 4, 5)            // %25-49: 3 LED
            else                      -> intArrayOf(5)                 // %0-24: 1 LED
        }
        
        var b = builder.buildPeriod(0)
        for (idx in indices) {
            b = b.buildChannel(idx)
        }
        mGM?.toggle(b.build())
    }

    // ─── AT_DOOR: Tüm ışıklar yanıp söner ───────────────────────────────────

    private fun blinkAll(speed: BlinkSpeed) {
        val builder = mGM?.getGlyphFrameBuilder() ?: return

        val frame: GlyphFrame = when {
            // Phone (1) — A, B, C, D, E
            Common.is20111() ->
                builder
                    .buildChannelA().buildChannelB().buildChannelC()
                    .buildChannelD().buildChannelE()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Phone (2) — A, B, C, D, E
            Common.is22111() ->
                builder
                    .buildChannelA().buildChannelB().buildChannelC()
                    .buildChannelD().buildChannelE()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Phone (2a) / (2a Plus) — A, B, C
            Common.is23111() || Common.is23113() ->
                builder
                    .buildChannelA().buildChannelB().buildChannelC()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Phone (3a) / (3a Pro) — A, B, C
            Common.is24111() ->
                builder
                    .buildChannelA().buildChannelB().buildChannelC()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Phone (4a) / (4a Pro) — Sadece A (index 0-5)
            Common.is25111() || Common.is25111p() ->
                builder
                    .buildChannel(0).buildChannel(1).buildChannel(2)
                    .buildChannel(3).buildChannel(4).buildChannel(5)
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // CMF Phone (1) — A, B, C
            Common.is23112() ->
                builder
                    .buildChannelA().buildChannelB().buildChannelC()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Bilinmeyen model — güvenli fallback
            else ->
                builder
                    .buildChannelA()
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()
        }

        mGM?.animate(frame)
    }

    companion object {
        private const val TAG = "GlyphDeliveryManager"
    }
}
