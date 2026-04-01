/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

enum class BlinkSpeed(
    val period: Int,
    val cycles: Int,
    val interval: Int,
    val label: String
) {
    FAST(200, 15, 100, "Hızlı"),
    NORMAL(400, 10, 200, "Normal"),
    SLOW(700, 6, 350, "Yavaş");

    companion object {
        fun fromName(name: String): BlinkSpeed =
            entries.firstOrNull { it.name == name } ?: NORMAL
    }
}
