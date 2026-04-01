/*
 * Delivery Glyph — Developer: Gdusunen
 *
 * Tracks delivery status per active order key (packageName:notificationId).
 * Returns the highest-priority status among all tracked orders.
 */
package com.nothing.deliveryglyph

object OrderTracker {

    // key = "packageName:notificationId"
    private val orders = mutableMapOf<String, DeliveryStatus>()

    var onStatusChanged: ((DeliveryStatus) -> Unit)? = null

    fun update(key: String, status: DeliveryStatus) {
        val prev = orders[key]
        if (prev == status) return
        orders[key] = status
        onStatusChanged?.invoke(highestStatus())
    }

    fun remove(key: String) {
        if (orders.remove(key) != null) {
            onStatusChanged?.invoke(highestStatus())
        }
    }

    fun clearAll() {
        orders.clear()
        onStatusChanged?.invoke(DeliveryStatus.IDLE)
    }

    /** Returns the highest-priority [DeliveryStatus] among all active orders, or [DeliveryStatus.IDLE]. */
    fun highestStatus(): DeliveryStatus =
        orders.values.maxByOrNull { it.ordinal } ?: DeliveryStatus.IDLE

    fun getOrders(): Map<String, DeliveryStatus> = orders.toMap()
}
