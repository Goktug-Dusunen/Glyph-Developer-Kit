# Delivery Glyph

> Real-time food delivery tracking through the Glyph lights and LED Matrix on your Nothing Phone — no screen needed.

**Developer:** [Göktuğ Düşünen](https://github.com/Goktug-Dusunen)

---

## What It Does

Delivery Glyph listens to notifications from food delivery apps (Getir, Yemek Sepeti, Trendyol, Migros and more) and reflects the order status directly on your Nothing Phone's Glyph interface. As your order progresses, the lights fill up — and when the courier arrives at your door, everything blinks.

On Matrix-capable devices (CMF Phone 1, Phone 4a Pro), the status is also displayed as text on the LED matrix simultaneously.

---

## Supported Devices

| Device | Model | Glyph | Matrix |
|---|---|---|---|
| Nothing Phone (1) | `20111` | A, B, C, D, E | — |
| Nothing Phone (2) | `22111` | A, B, C, D, E | — |
| Nothing Phone (2a) | `23111` | A, B, C | — |
| Nothing Phone (2a) Plus | `23113` | A, B, C | — |
| Nothing Phone (3a) / 3a Pro | `24111` | A, B, C | — |
| Nothing Phone (4a) | `25111` | A (6 LEDs) | — |
| **CMF Phone (1)** | `23112` | A, B, C | **Yes** |
| **Nothing Phone (4a) Pro** | `25111p` | A (6 LEDs) | **Yes** |

---

## Supported Delivery Apps

| App | Package Name |
|---|---|
| Getir | `co.getir.app` |
| Yemek Sepeti | `com.yemeksepeti.android` |
| Trendyol | `com.trendyol.client` |
| Migros | `com.migros.migrosone` |

You can add **any other delivery app** from the Settings screen by entering its package name.

---

## Glyph Behavior

### Order Progress

| Stage | Glyph Fill | Matrix Text |
|---|---|---|
| Order Confirmed | 25% | `ONAY` |
| Preparing | 50% | `HAZR` |
| On the Way | 75% | `YOLDA` |
| **At Your Door** | **All blink** | **`KAPI!`** |

### Per-Model Details

**Phone (1)** — Uses the D channel (D1_1–D1_8) for progress. All channels blink on arrival.

**Phone (2)** — Uses the C channel (C1_1–C1_16) for progress. All channels blink on arrival.

**Phone (2a) / (2a Plus) / (3a) / (3a Pro)** — Uses the C channel for progress. A+B+C blink on arrival.

**Phone (4a) / (4a Pro)** — Uses 6 individual A-channel LEDs, filled from bottom to top:
```
 0–24%  →  □ □ □ □ □ ■   (1 LED)
25–49%  →  □ □ □ ■ ■ ■   (3 LEDs)
50–74%  →  □ ■ ■ ■ ■ ■   (5 LEDs)
75–100% →  ■ ■ ■ ■ ■ ■   (6 LEDs, blinks at door)
```

**CMF Phone (1) / Phone (4a) Pro** — Regular Glyph LEDs work as above **plus** the LED matrix simultaneously shows the delivery status as text.

### Blink Speed

You can choose the blink speed for the "At Your Door" animation from the Settings screen:

| Speed | Period | Cycles |
|---|---|---|
| Fast | 200ms | 15 |
| Normal | 400ms | 10 |
| Slow | 700ms | 6 |

---

## Features

- **Real-time tracking** — status updates the moment a notification arrives
- **Glyph Matrix support** — text display on CMF Phone 1 and Phone 4a Pro
- **Multi-order tracking** — tracks multiple orders simultaneously (e.g. Getir + Yemek Sepeti at the same time), always showing the highest-priority status
- **Custom app support** — add any delivery app by package name from the Settings screen
- **Per-app toggle** — enable or disable individual apps
- **Blink speed setting** — Fast / Normal / Slow
- **Notification history** — last 10 delivery events with timestamps
- **Auto-reset** — Glyph and Matrix turn off automatically 30 seconds after delivery or 3 seconds after the notification is dismissed

---

## Installation

### Requirements

- Android 14 or higher
- A supported Nothing or CMF device (see table above)
- Android Studio (Hedgehog or newer recommended)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Goktug-Dusunen/Glyph-Developer-Kit.git
cd Glyph-Developer-Kit/DeliveryGlyph
```

### Step 2 — Copy the SDK

```bash
cp ../sdk/glyph-matrix-sdk-2.0.aar app/libs/
```

### Step 3 — Open in Android Studio

Open the `DeliveryGlyph/` folder in Android Studio and wait for Gradle to sync.

### Step 4 — Enable Debug Mode

During development, the API key in `AndroidManifest.xml` is already set to `test`. Enable debug mode via ADB:

```bash
adb shell settings put global nt_glyph_interface_debug_enable 1
```

> Debug mode automatically disables after 48 hours.

### Step 5 — Install & Grant Permission

1. Run the app on your Nothing device
2. Tap **Grant Permission**
3. Find **Delivery Glyph** in the Notification Access list and enable it
4. Return to the app — status should show as active

### Step 6 — Production API Key (optional)

If you're distributing the app, replace the test key in `AndroidManifest.xml`:

```xml
<!-- Inside the <application> tag -->
<meta-data android:name="NothingKey" android:value="YOUR_API_KEY" />
```

Apply for an API key at the [Nothing Developer Programme](https://nothing.tech/pages/developer).

> **Note:** API key restriction is removed starting from Android 16. The `NothingKey` meta-data is still recommended for compatibility.

---

## Project Structure

```
DeliveryGlyph/
└── app/src/main/java/com/nothing/deliveryglyph/
    ├── MainActivity.kt                  # Main screen, permission check, device detection
    ├── SettingsActivity.kt              # Blink speed, app toggles, custom apps
    ├── HistoryActivity.kt               # Last 10 notification events
    ├── DeliveryNotificationListener.kt  # Notification listener service
    ├── GlyphDeliveryManager.kt          # Glyph LED control (all models)
    ├── GlyphMatrixDeliveryManager.kt    # Glyph Matrix control (CMF / 4a Pro)
    ├── OrderTracker.kt                  # Multi-order state management
    ├── DeliveryStatus.kt                # Status enum + notification parser
    ├── AppSettings.kt                   # SharedPreferences wrapper
    ├── AppEntry.kt                      # App model
    ├── BlinkSpeed.kt                    # Blink speed enum
    └── NotificationEvent.kt            # History event model
```

---

## License

MIT License — Göktuğ Düşünen
