/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

data class AppEntry(
    val packageName: String,
    val displayName: String,
    val isBuiltIn: Boolean,
    var isEnabled: Boolean = true
)
