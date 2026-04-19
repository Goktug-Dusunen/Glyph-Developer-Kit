/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nothing.ketchum.Common

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(applicationContext)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnGrantPermission).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val granted   = isNotificationListenerEnabled()
        val tvStatus  = findViewById<TextView>(R.id.tvStatus)
        val tvDevice  = findViewById<TextView>(R.id.tvDevice)
        val btnGrant  = findViewById<Button>(R.id.btnGrantPermission)

        tvDevice.text = detectDevice()

        if (granted) {
            tvStatus.text = "Bildirim izni: Aktif ✓\n\nGetir, Yemek Sepeti ve diğer\nteslimat bildirimleri Glyph'e yansıtılıyor."
            btnGrant.text = "İzin Ayarlarını Aç"
        } else {
            tvStatus.text = "Bildirim izni gerekli.\nAşağıdaki butona basarak izin verin."
            btnGrant.text = "İzin Ver"
        }
    }

    private fun detectDevice(): String = when {
        Common.is20111()  -> "Nothing Phone (1)"
        Common.is22111()  -> "Nothing Phone (2)"
        Common.is23111()  -> "Nothing Phone (2a)"
        Common.is23112()  -> "CMF Phone (1) — Glyph + Matrix"
        Common.is23113()  -> "Nothing Phone (2a) Plus"
        Common.is24111()  -> "Nothing Phone (3a) / 3a Pro"
        Common.is25111()  -> "Nothing Phone (4a)"
        Common.is25111p() -> "Nothing Phone (4a) Pro — Glyph + Matrix"
        else              -> "Bilinmeyen / Desteklenmeyen Cihaz"
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return !TextUtils.isEmpty(flat) && flat.contains(packageName)
    }
}
