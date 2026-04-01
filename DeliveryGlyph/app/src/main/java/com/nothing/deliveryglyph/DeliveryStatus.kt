/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

/**
 * Teslimat aşamaları.
 * Enum sırasına göre öncelik belirlenir (ordinal büyük = daha öncelikli).
 *   IDLE < CONFIRMED < PREPARING < ON_THE_WAY < AT_DOOR
 */
enum class DeliveryStatus(val progress: Int, val label: String) {
    IDLE(0,    "Beklemede"),
    CONFIRMED(25,  "Sipariş Onaylandı"),   // Glyph: %25
    PREPARING(50,  "Hazırlanıyor"),         // Glyph: %50
    ON_THE_WAY(75, "Yolda"),               // Glyph: %75
    AT_DOOR(-1,    "Kapıda!")              // Glyph: yanıp söner
}

object DeliveryParser {

    /** Built-in desteklenen uygulamalar. Settings ekranı bu listeyi kullanır. */
    val ALL_APPS: List<AppEntry> = listOf(
        AppEntry("co.getir.app",              "Getir",         isBuiltIn = true),
        AppEntry("com.yemeksepeti.android",   "Yemek Sepeti",  isBuiltIn = true),
        AppEntry("com.trendyol.client",       "Trendyol",      isBuiltIn = true),
        AppEntry("com.migros.migrosone",      "Migros",        isBuiltIn = true)
    )

    private val BUILTIN_PACKAGES: Set<String> = ALL_APPS.map { it.packageName }.toSet()

    fun isBuiltInApp(packageName: String): Boolean = packageName in BUILTIN_PACKAGES

    /**
     * Bildirimdeki başlık ve metni analiz ederek teslimat aşamasını döndürür.
     * En spesifik eşleşme önce kontrol edilir.
     */
    fun parse(title: String?, text: String?): DeliveryStatus? {
        val combined = "${title.orEmpty()} ${text.orEmpty()}".lowercase()

        return when {

            // ── Kapıda / Teslim ──────────────────────────────────────────────
            combined.containsAny(
                "kapınızda", "kapinda",
                "teslim edildi", "teslim edilmistir",
                "kurye kapı", "kurye kapi",
                "adreste", "adresinizde",
                "kapıya geldi", "kapiya geldi",
                "sipariş teslim",
                "delivered", "at your door"
            ) -> DeliveryStatus.AT_DOOR

            // ── Yolda ────────────────────────────────────────────────────────
            combined.containsAny(
                "yola çıktı", "yola cikti",
                "yola çıkıldı", "yola cikildi",
                "yolda", "kurye yolda",
                "kurye yola", "kurye atandı", "kurye atandi",
                "out for delivery", "on the way",
                "yöneldi", "yoneldi",
                "trendyol express yola"
            ) -> DeliveryStatus.ON_THE_WAY

            // ── Hazırlanıyor ─────────────────────────────────────────────────
            combined.containsAny(
                "hazırlanıyor", "hazirlaniyor",
                "hazırlanmaya başlandı", "hazirlanmaya baslandi",
                "restoran hazırlıyor", "mutfakta",
                "siparişiniz hazır", "siparissiniz hazir",
                "preparing", "being prepared"
            ) -> DeliveryStatus.PREPARING

            // ── Onaylandı ────────────────────────────────────────────────────
            combined.containsAny(
                "onaylandı", "onaylandi",
                "alındı", "alindi",
                "siparişiniz alındı",
                "confirmed", "accepted",
                "kabul edildi",
                "sipariş oluşturuldu", "siparis olusturuldu"
            ) -> DeliveryStatus.CONFIRMED

            else -> null
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it) }
}
