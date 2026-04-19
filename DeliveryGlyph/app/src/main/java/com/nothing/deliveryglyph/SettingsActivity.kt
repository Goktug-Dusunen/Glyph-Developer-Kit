/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var llAppSwitches: LinearLayout
    private lateinit var llCustomApps: LinearLayout
    private lateinit var rgBlinkSpeed: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(applicationContext)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            title = "Ayarlar"
            setDisplayHomeAsUpEnabled(true)
            elevation = 0f
        }

        llAppSwitches = findViewById(R.id.llAppSwitches)
        llCustomApps  = findViewById(R.id.llCustomApps)
        rgBlinkSpeed  = findViewById(R.id.rgBlinkSpeed)

        buildBuiltInSwitches()
        buildCustomAppRows()
        setupBlinkSpeedRadio()

        findViewById<Button>(R.id.btnAddApp).setOnClickListener { showAddAppDialog() }
        findViewById<Button>(R.id.btnClearHistory).setOnClickListener {
            AppSettings.clearHistory()
            showToast("Geçmiş temizlendi.")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    // ── Built-in uygulama switch'leri ────────────────────────────────────────

    private fun buildBuiltInSwitches() {
        llAppSwitches.removeAllViews()
        DeliveryParser.ALL_APPS.forEach { app ->
            val row = makeSwitchRow(
                label      = app.displayName,
                checked    = AppSettings.isAppEnabled(app.packageName),
                removable  = false,
                onChange   = { isChecked ->
                    AppSettings.setAppEnabled(app.packageName, isChecked)
                }
            )
            llAppSwitches.addView(row)
        }
    }

    // ── Özel uygulama satırları ───────────────────────────────────────────────

    private fun buildCustomAppRows() {
        llCustomApps.removeAllViews()
        val customs = AppSettings.getCustomApps()
        if (customs.isEmpty()) {
            val hint = TextView(this).apply {
                text = "Henüz özel uygulama eklenmedi."
                textSize = 13f
                setTextColor(0xFFAAAAAA.toInt())
                setPadding(8, 8, 8, 8)
            }
            llCustomApps.addView(hint)
            return
        }
        customs.forEach { app ->
            val row = makeSwitchRow(
                label      = "${app.displayName}\n${app.packageName}",
                checked    = app.isEnabled,
                removable  = true,
                onChange   = { isChecked ->
                    AppSettings.setCustomAppEnabled(app.packageName, isChecked)
                },
                onRemove   = {
                    AppSettings.removeCustomApp(app.packageName)
                    buildCustomAppRows()
                }
            )
            llCustomApps.addView(row)
        }
    }

    // ── Animasyon hızı radio ─────────────────────────────────────────────────

    private fun setupBlinkSpeedRadio() {
        val current = AppSettings.getBlinkSpeed()
        rgBlinkSpeed.check(when (current) {
            BlinkSpeed.FAST   -> R.id.rbFast
            BlinkSpeed.NORMAL -> R.id.rbNormal
            BlinkSpeed.SLOW   -> R.id.rbSlow
        })
        rgBlinkSpeed.setOnCheckedChangeListener { _, id ->
            val speed = when (id) {
                R.id.rbFast -> BlinkSpeed.FAST
                R.id.rbSlow -> BlinkSpeed.SLOW
                else        -> BlinkSpeed.NORMAL
            }
            AppSettings.setBlinkSpeed(speed)
        }
    }

    // ── Özel uygulama ekleme dialog'u ────────────────────────────────────────

    private fun showAddAppDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
        }
        val etPackage = EditText(this).apply {
            hint = "Paket adı (ör: com.example.app)"
        }
        val etName = EditText(this).apply {
            hint = "Uygulama adı (ör: Sipariş Takip)"
        }
        layout.addView(etPackage)
        layout.addView(etName)

        AlertDialog.Builder(this)
            .setTitle("Özel Uygulama Ekle")
            .setView(layout)
            .setPositiveButton("Ekle") { _, _ ->
                val pkg  = etPackage.text.toString().trim()
                val name = etName.text.toString().trim().ifBlank { pkg }
                if (pkg.isNotEmpty() && pkg.contains('.')) {
                    AppSettings.addCustomApp(pkg, name)
                    buildCustomAppRows()
                } else {
                    showToast("Geçerli bir paket adı girin (nokta içermeli).")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    // ── Yardımcılar ───────────────────────────────────────────────────────────

    private fun makeSwitchRow(
        label: String,
        checked: Boolean,
        removable: Boolean,
        onChange: (Boolean) -> Unit,
        onRemove: (() -> Unit)? = null
    ): View {
        val dp = resources.displayMetrics.density

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            val padV = (14 * dp).toInt()
            val padH = (16 * dp).toInt()
            setPadding(padH, padV, padH, padV)
        }

        val tv = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val sw = SwitchCompat(this).apply {
            isChecked = checked
            trackTintList = android.content.res.ColorStateList.valueOf(0xFF2E2E2E.toInt())
            thumbTintList = android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(0xFFFFFFFF.toInt(), 0xFF666666.toInt())
            )
            setOnCheckedChangeListener { _, isChecked -> onChange(isChecked) }
        }

        row.addView(tv)
        row.addView(sw)

        if (removable && onRemove != null) {
            val btnRemove = TextView(this).apply {
                text = "✕"
                textSize = 14f
                setTextColor(0xFFF87171.toInt())
                setPadding((16 * dp).toInt(), 0, 0, 0)
                setOnClickListener { onRemove() }
            }
            row.addView(btnRemove)
        }

        return row
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
