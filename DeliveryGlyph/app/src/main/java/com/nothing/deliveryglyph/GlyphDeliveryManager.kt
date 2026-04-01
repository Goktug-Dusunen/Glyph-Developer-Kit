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
    private var isSessionOpen = false
    private var currentStatus = DeliveryStatus.IDLE

    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            try {
                when {
                    Common.is20111() -> mGM?.register(Glyph.DEVICE_20111)
                    Common.is22111() -> mGM?.register(Glyph.DEVICE_22111)
                    Common.is23111() -> mGM?.register(Glyph.DEVICE_23111)
                    Common.is23113() -> mGM?.register(Glyph.DEVICE_23113)
                    Common.is24111() -> mGM?.register(Glyph.DEVICE_24111)
                    Common.is25111() -> mGM?.register(Glyph.DEVICE_25111)
                }
                mGM?.openSession()
                isSessionOpen = true
                Log.d(TAG, "Glyph session açıldı")
            } catch (e: GlyphException) {
                Log.e(TAG, "Session açılamadı: ${e.message}")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            isSessionOpen = false
            mGM?.closeSession()
        }
    }

    fun init() {
        mGM = GlyphManager.getInstance(context)
        mGM?.init(callback)
    }

    fun destroy() {
        try {
            if (isSessionOpen) mGM?.closeSession()
        } catch (e: GlyphException) {
            Log.e(TAG, e.message ?: "")
        }
        mGM?.unInit()
        isSessionOpen = false
    }

    /**
     * @param status Yeni teslimat aşaması
     * @param blinkSpeed AT_DOOR animasyonu için hız ayarı
     */
    fun applyStatus(status: DeliveryStatus, blinkSpeed: BlinkSpeed = BlinkSpeed.NORMAL) {
        if (!isSessionOpen) return
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
        try {
            if (isSessionOpen) mGM?.turnOff()
        } catch (e: GlyphException) {
            Log.e(TAG, e.message ?: "")
        }
    }

    // ─── Progress gösterimi ───────────────────────────────────────────────────

    private fun showProgress(progress: Int) {
        when {
            Common.is25111() -> showProgressPhone4a(progress)
            Common.is20111() -> showProgressWithChannel(progress, useD = true)
            else             -> showProgressWithChannel(progress, useD = false)
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
        mGM?.displayProgress(frame, progress)
    }

    /**
     * Phone (4a) — Sadece A1-A6 (index 0-5).
     * displayProgress C/D kanalı beklediği için manuel LED kontrolü.
     *
     * A1=top=index0, A6=bottom=index5 — alttan üste doldurur:
     *   25%  → [5]          = 1 LED
     *   50%  → [3,4,5]      = 3 LED
     *   75%  → [1,2,3,4,5]  = 5 LED
     */
    private fun showProgressPhone4a(progress: Int) {
        val builder = mGM?.getGlyphFrameBuilder() ?: return
        val indices: IntArray = when {
            progress <= 25 -> intArrayOf(5)
            progress <= 50 -> intArrayOf(3, 4, 5)
            else           -> intArrayOf(1, 2, 3, 4, 5)
        }
        var b = builder
        for (idx in indices) b = b.buildChannel(idx)
        mGM?.toggle(b.buildPeriod(0).build())
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
            Common.is23111(), Common.is23113() ->
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

            // Phone (4a) — Sadece A (index 0-5)
            Common.is25111() ->
                builder
                    .buildChannel(0).buildChannel(1).buildChannel(2)
                    .buildChannel(3).buildChannel(4).buildChannel(5)
                    .buildPeriod(speed.period).buildCycles(speed.cycles)
                    .buildInterval(speed.interval).build()

            // Bilinmeyen model
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
