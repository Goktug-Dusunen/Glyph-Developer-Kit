# Katkı Rehberi — Delivery Glyph

## Yeni Teslimat Uygulaması Eklemek

### Adım 1 — Paket Adını Bul
Uygulamayı Play Store'da bul, URL'deki `id=` parametresi paket adıdır.
Alternatif: telefonda `adb shell pm list packages | grep <kismad>`

### Adım 2 — Built-in Listeye Ekle
`DeliveryStatus.kt` içinde `DeliveryParser.ALL_APPS` listesine ekle:

```kotlin
val ALL_APPS: List<AppEntry> = listOf(
    // ... mevcut uygulamalar ...
    AppEntry("com.yeniuygulama.android", "Uygulama Adı", isBuiltIn = true),
)
```

### Adım 3 — Keyword'leri Test Et
Uygulamadan gerçek bir sipariş ver ve gelen bildirimlerin metin içeriğini logcat ile yakala:

```bash
adb logcat -s DeliveryGlyphListener
```

Bildirim metni mevcut keyword'leri kapsıyorsa ek bir şey gerekmez.
Kapsamıyorsa `DeliveryParser.parse()` içinde yeni `when` dalı veya mevcut bloklara keyword ekle:

```kotlin
combined.containsAny(
    "yeni_keyword_1", "yeni_keyword_2"
) -> DeliveryStatus.ON_THE_WAY
```

### Adım 4 — PR Gönder
- Branch adı: `feat/add-<uygulama-adi>`
- Commit'e paket adını ve hangi bildirimleri test ettiğini yaz
- `README.md` desteklenen uygulamalar tablosunu güncelle

---

## Mevcut Keyword'leri Genişletmek

`DeliveryStatus.kt` → `DeliveryParser.parse()` fonksiyonunu düzenle.
Her `when` dalı sıralı kontrol edilir; daha spesifik olanlar üstte olmalı.

---

## Yeni Model Desteği

Nothing yeni bir Phone modeli çıkardığında:

1. `GlyphDeliveryManager.kt` → `callback.onServiceConnected()` içine `register` satırı ekle
2. `blinkAll()` içine yeni `when` dalı ekle (mevcut kanalları belirt)
3. `showProgress()` içine gerekirse özel progress mantığı ekle
4. `MainActivity.kt` → `detectDevice()` fonksiyonuna ekle
5. `README.md` model tablosunu güncelle

---

## Özel Uygulama Ekleme (Kullanıcı Tarafı)

Kaynak kodu değiştirmeden: Uygulama → **Ayarlar** → **Uygulama Ekle** → paket adını ve adını gir.
