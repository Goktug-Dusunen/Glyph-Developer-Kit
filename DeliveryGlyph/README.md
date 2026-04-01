# Delivery Glyph

**Nothing Phone Glyph ışıklarını teslimat uygulaması bildirimlerine göre kontrol eder.**

Getir, Yemek Sepeti, Trendyol ve Migros siparişlerinizin aşamalarını telefonunuzun arka yüzündeki Glyph ışıklarıyla takip edin.

> **Developer:** [Gdusunen](https://github.com/Gdusunen)

---

## Desteklenen Uygulamalar

| Uygulama | Paket Adı |
|---|---|
| Getir | `co.getir.app` |
| Yemek Sepeti | `com.yemeksepeti.android` |
| Trendyol | `com.trendyol.client` |
| Migros | `com.migros.migrosone` |

Ayarlar ekranından istediğiniz uygulamayı ekleyebilirsiniz.

---

## Glyph Davranışı

| Aşama | Glyph |
|---|---|
| Sipariş onaylandı | %25 doluluk |
| Hazırlanıyor | %50 doluluk |
| Yolda | %75 doluluk |
| **Kapıda** | **Tüm ışıklar yanıp söner** |

### Model Bazlı Kanallar

| Model | Kanallar | Progress | Blink |
|---|---|---|---|
| Phone (1) `20111` | A,B,C,D,E | D kanalı | Tümü |
| Phone (2) `22111` | A,B,C,D,E | C kanalı | Tümü |
| Phone (2a) `23111` | A,B,C | C kanalı | A+B+C |
| Phone (2a) Plus `23113` | A,B,C | C kanalı | A+B+C |
| Phone (3a) / Pro `24111` | A,B,C | C kanalı | A+B+C |
| **Phone (4a) `25111`** | **A (6 LED)** | Manuel LED | A0-A5 |

Phone (4a) için A kanalındaki 6 LED alttan üste dolar:
```
25%  →  □ □ □ □ □ ■   (1 LED)
50%  →  □ □ □ ■ ■ ■   (3 LED)
75%  →  □ ■ ■ ■ ■ ■   (5 LED)
AT_DOOR → ■ ■ ■ ■ ■ ■  (yanıp söner)
```

---

## Kurulum

### 1. SDK'yı Kopyala
```bash
cp ../sdk/glyph-matrix-sdk-2.0.aar app/libs/
```

### 2. Android Studio'da Aç
`DeliveryGlyph/` klasörünü Android Studio ile açın.

### 3. Debug Modu (geliştirme sırasında)
`AndroidManifest.xml` içindeki API key zaten `test` olarak ayarlı. Ek olarak:
```bash
adb shell settings put global nt_glyph_interface_debug_enable 1
```
> Debug modu 48 saat sonra otomatik devre dışı olur.

### 4. İzin Ver
Uygulamayı yükledikten sonra açın → **İzin Ver** butonuna basın → Açılan ekranda Delivery Glyph'i etkinleştirin.

### 5. Production API Key
```xml
<!-- AndroidManifest.xml → <application> etiketi içine -->
<meta-data android:name="NothingKey" android:value="GERCEK_API_KEY" />
```

---

## Özellikler

- Tüm Nothing Phone modellerini destekler (Phone 1–4a)
- Eşzamanlı çoklu sipariş takibi (Getir + Yemek Sepeti aynı anda)
- Animasyon hızı ayarı (Hızlı / Normal / Yavaş)
- Özel uygulama ekleme (herhangi bir teslimat uygulaması)
- Son 10 bildirimin geçmişi
- Per-app etkinleştirme/devre dışı bırakma

---

## Katkı

[CONTRIBUTING.md](CONTRIBUTING.md) dosyasına bakın.

---

## Lisans

MIT License — Gdusunen
