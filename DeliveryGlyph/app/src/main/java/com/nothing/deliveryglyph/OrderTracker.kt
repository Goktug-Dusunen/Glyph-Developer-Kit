
/*
 * Delivery Glyph — Developer: Gdusunen
 *
 * Tracks delivery status per active order key (packageName:notificationId).
 * Returns the highest-priority status among all tracked orders.
 * Thread-safe implementation.
 */
package com.nothing.deliveryglyph

import java.util.concurrent.ConcurrentHashMap

object OrderTracker {

    // Thread-safe map: key = "packageName:notificationId"
    private val orders = ConcurrentHashMap<String, DeliveryStatus>()

    @Volatile
    var onStatusChanged: ((DeliveryStatus) -> Unit)? = null

    fun update(key: String, status: DeliveryStatus) {
        val prev = orders.put(key, status)
        if (prev != status) {
            onStatusChanged?.invoke(highestStatus())
        }
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
    
    fun getOrderCount(): Int = orders.size
}
