<div align="center">

# Delivery Glyph

### Real-time delivery progress on the Nothing Phone Glyph Interface

[![Build & Release APK](https://github.com/Goktug-Dusunen/Glyph-Developer-Kit/actions/workflows/build.yml/badge.svg)](https://github.com/Goktug-Dusunen/Glyph-Developer-Kit/actions/workflows/build.yml)

[App documentation](DeliveryGlyph/README.md) · [Glyph SDK reference](SDK.md) · [Contributing](DeliveryGlyph/CONTRIBUTING.md)

</div>

Delivery Glyph turns notifications from Turkish delivery services into glanceable progress on Nothing Phone Glyph lights. Order confirmation, preparation, courier progress, and arrival are mapped to a tiered LED fill; supported Matrix devices also display short status text.

<p align="center">
  <img src="assets/Frame5-transparent.png" width="22%" alt="Delivery Glyph app screen" />
  <img src="assets/Frame6-transparent.png" width="22%" alt="Delivery Glyph tracking screen" />
  <img src="assets/Frame8-transparent.png" width="22%" alt="Delivery Glyph settings screen" />
</p>

## Highlights

- Notification-based tracking for Getir, Yemeksepeti, Trendyol, and Migros
- Custom delivery apps and per-app controls
- Nothing Phone (1), (2), (2a), (3a), and (4a) family support
- Glyph Matrix output for CMF Phone (1) and Nothing Phone (4a) Pro
- Multiple simultaneous orders with priority-aware status selection
- Configurable arrival animation and local notification history
- Fully on-device operation with no internet permission or analytics dependency

## Delivery stages

| Stage | Glyph behavior | Matrix text |
|---|---|---|
| Confirmed | 25% fill | `ONAY` |
| Preparing | 50% fill | `HAZR` |
| On the way | 75% fill | `YOLDA` |
| At the door | Full blink | `KAPI!` |

## Repository layout

| Path | Purpose |
|---|---|
| [`DeliveryGlyph/`](DeliveryGlyph/) | Android application and complete setup guide |
| [`sdk/`](sdk/) | Nothing Glyph Matrix SDK binary used by the sample app |
| [`SDK.md`](SDK.md) | Upstream Glyph SDK integration and API reference |
| [`.github/workflows/build.yml`](.github/workflows/build.yml) | Reproducible Android build and tagged APK release workflow |

## Quick start

```bash
git clone https://github.com/Goktug-Dusunen/Glyph-Developer-Kit.git
cd Glyph-Developer-Kit/DeliveryGlyph
cp ../sdk/glyph-matrix-sdk-2.0.aar app/libs/
gradle assembleDebug --no-daemon
```

See the [application documentation](DeliveryGlyph/README.md) for device support, notification access, debug mode, production API keys, and architecture details.

## Project relationship

This repository is based on the Nothing Developer Programme's Glyph Developer Kit. The `DeliveryGlyph` application, its product documentation, and its automation are maintained by [Göktuğ Düşünen](https://github.com/Goktug-Dusunen). Upstream SDK components retain their respective terms.

## License

The Delivery Glyph application code is available under the [MIT License](DeliveryGlyph/LICENSE). Bundled SDK components and upstream documentation retain their original licensing terms.
