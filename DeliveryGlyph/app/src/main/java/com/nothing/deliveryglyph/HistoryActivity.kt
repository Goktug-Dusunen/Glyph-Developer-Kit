/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(applicationContext)
        setContentView(R.layout.activity_history)

        supportActionBar?.apply {
            title = "Bildirim Geçmişi"
            setDisplayHomeAsUpEnabled(true)
        }

        rvHistory = findViewById(R.id.rvHistory)
        tvEmpty   = findViewById(R.id.tvEmpty)

        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val events = AppSettings.getHistory()
        if (events.isEmpty()) {
            rvHistory.visibility = View.GONE
            tvEmpty.visibility   = View.VISIBLE
        } else {
            tvEmpty.visibility   = View.GONE
            rvHistory.visibility = View.VISIBLE
            rvHistory.layoutManager = LinearLayoutManager(this)
            rvHistory.adapter = HistoryAdapter(events)
            if (rvHistory.itemDecorationCount == 0) {
                rvHistory.addItemDecoration(
                    DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, "Temizle")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            MENU_CLEAR -> {
                AppSettings.clearHistory()
                loadHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val MENU_CLEAR = 1
    }
}
