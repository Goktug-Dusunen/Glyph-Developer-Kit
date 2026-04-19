/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nothing.ketchum.Common

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(applicationContext)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        findViewById<TextView>(R.id.btnGrantPermission).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        findViewById<TextView>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<TextView>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        setupStages()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val granted  = isNotificationListenerEnabled()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDevice = findViewById<TextView>(R.id.tvDevice)
        val btnGrant = findViewById<TextView>(R.id.btnGrantPermission)
        val cardPerm = findViewById<View>(R.id.cardPermission)
        val dot      = findViewById<View>(R.id.statusDot)

        tvDevice.text = detectDevice()

        if (granted) {
            tvStatus.text = "Aktif — bildirimler izleniyor"
            btnGrant.text = "İzin Ayarlarını Aç"
            cardPerm.setBackgroundResource(R.drawable.bg_permission_active)
            dot.setBackgroundColor(0xFF4ADE80.toInt())
        } else {
            tvStatus.text = "İzin gerekli"
            btnGrant.text = "İzin Ver →"
            cardPerm.setBackgroundResource(R.drawable.bg_permission_inactive)
            dot.setBackgroundColor(0xFFF87171.toInt())
        }
    }

    private fun setupStages() {
        data class Stage(val id: Int, val icon: String, val label: String, val desc: String, val fill: String)

        val stages = listOf(
            Stage(R.id.stageConfirmed,  "✓", "Sipariş Onaylandı",  "Onaylandı",    "%25"),
            Stage(R.id.stagePreparing,  "◎", "Hazırlanıyor",        "Hazırlanıyor", "%50"),
            Stage(R.id.stageOnTheWay,   "→", "Yolda",              "Yolda",        "%75"),
            Stage(R.id.stageAtDoor,     "!", "Kapıda!",            "Yanıp söner",  "blink")
        )

        stages.forEach { s ->
            val view = findViewById<View>(s.id)
            view.findViewById<TextView>(R.id.tvStageIcon).text  = s.icon
            view.findViewById<TextView>(R.id.tvStageLabel).text = s.label
            view.findViewById<TextView>(R.id.tvStageDesc).text  = s.desc
            view.findViewById<TextView>(R.id.tvStageFill).text  = s.fill
        }
    }

    private fun detectDevice(): String = when {
        Common.is20111()  -> "Nothing Phone (1)  ·  20111"
        Common.is22111()  -> "Nothing Phone (2)  ·  22111"
        Common.is23111()  -> "Nothing Phone (2a)  ·  23111"
        Common.is23112()  -> "CMF Phone (1)  ·  23112  ·  MATRIX"
        Common.is23113()  -> "Nothing Phone (2a) Plus  ·  23113"
        Common.is24111()  -> "Nothing Phone (3a)  ·  24111"
        Common.is25111()  -> "Nothing Phone (4a)  ·  25111"
        Common.is25111p() -> "Nothing Phone (4a) Pro  ·  25111p  ·  MATRIX"
        else              -> "Bilinmeyen Cihaz"
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return !TextUtils.isEmpty(flat) && flat.contains(packageName)
    }
}
