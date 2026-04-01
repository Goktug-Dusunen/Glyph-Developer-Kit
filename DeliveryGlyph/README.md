# Delivery Glyph

**Controls Nothing Phone Glyph lights based on food delivery app notifications.**

Track your Getir, Yemek Sepeti, Trendyol, and Migros orders through the Glyph lights on the back of your Nothing Phone — no need to check your screen.

> **Developer:** [Göktuğ Düşünen](https://github.com/Goktug-Dusunen)

---

## Supported Apps

| App | Package Name |
|---|---|
| Getir | `co.getir.app` |
| Yemek Sepeti | `com.yemeksepeti.android` |
| Trendyol | `com.trendyol.client` |
| Migros | `com.migros.migrosone` |

You can add any other delivery app from the Settings screen.

---

## Glyph Behavior

| Stage | Glyph |
|---|---|
| Order confirmed | 25% fill |
| Preparing | 50% fill |
| On the way | 75% fill |
| **At your door** | **All lights blink** |

### Per-Model Channel Mapping

| Model | Channels | Progress | Blink |
|---|---|---|---|
| Phone (1) `20111` | A,B,C,D,E | D channel | All |
| Phone (2) `22111` | A,B,C,D,E | C channel | All |
| Phone (2a) `23111` | A,B,C | C channel | A+B+C |
| Phone (2a) Plus `23113` | A,B,C | C channel | A+B+C |
| Phone (3a) / Pro `24111` | A,B,C | C channel | A+B+C |
| **Phone (4a) `25111`** | **A (6 LEDs)** | Manual LED | A0–A5 |

Phone (4a) fills the 6 A-channel LEDs from bottom to top:
```
25%     →  □ □ □ □ □ ■   (1 LED)
50%     →  □ □ □ ■ ■ ■   (3 LEDs)
75%     →  □ ■ ■ ■ ■ ■   (5 LEDs)
At door →  ■ ■ ■ ■ ■ ■   (blinks)
```

---

## Setup

### 1. Copy the SDK
```bash
cp ../sdk/glyph-matrix-sdk-2.0.aar app/libs/
```

### 2. Open in Android Studio
Open the `DeliveryGlyph/` folder in Android Studio and let it sync.

### 3. Debug Mode (during development)
The API key in `AndroidManifest.xml` is already set to `test`. Additionally run:
```bash
adb shell settings put global nt_glyph_interface_debug_enable 1
```
> Debug mode automatically disables after 48 hours.

### 4. Grant Permission
Install the app → tap **Grant Permission** → enable Delivery Glyph in the Notification Access screen.

### 5. Production API Key
```xml
<!-- Inside the <application> tag in AndroidManifest.xml -->
<meta-data android:name="NothingKey" android:value="YOUR_API_KEY" />
```

---

## Features

- Supports all Nothing Phone models (Phone 1 through Phone 4a)
- Simultaneous multi-order tracking (e.g. Getir + Yemek Sepeti at the same time)
- Blink speed setting — Fast / Normal / Slow
- Custom app support — add any delivery app by package name
- Last 10 notification events history
- Per-app enable / disable toggle

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License

MIT License — Göktuğ Düşünen
