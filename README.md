<div align="center">

<img src=".github/assets/logo.svg" width="128" alt="TeleVip logo">

# TeleVip

**Privacy, media and quality-of-life features for Telegram and its forks, as an Xposed module.**

[![Release](https://img.shields.io/github/v/release/2B-4G10/TeleVIP?label=release&color=FFD500)](../../releases/latest)
[![Xposed API](https://img.shields.io/badge/libxposed-API%20102-4A4D54)](#requirements)
[![License](https://img.shields.io/badge/license-GPL--3.0-F99B1C)](LICENSE)

[Download](../../releases/latest) · [Changelog](CHANGELOG.md) · [Telegram channel](https://t.me/t_l0_e)

</div>

---

## Features

| Privacy | Media & stories | Chats |
|---|---|---|
| Ghost Mode: no *seen*, *typing* or *online* | Save protected stories | Remove content-saving restrictions |
| Hide phone number | Save voice messages | Save message edit history |
| Hide story views | Save secret media | Hide pinned messages |
| Show deleted messages | Always allow saving media | Disable stories |
| Keep secret media from self-destructing | Faster downloads | Disable channel / profile swipe-back |
| Show user IDs on profiles | Local Premium | Jump to first / any message |

…and more in the module's settings, which live inside the client's own **Settings**.

## Requirements

- **LSPosed 1.10+** or **Vector 2.2+** (libxposed API 102). LSPosed 1.9.x, EdXposed and LSPatch are not supported.
- Any Zygisk provider: Magisk, KernelSU, Zygisk Next or NeoZygisk.
- Android 8.1 or newer.

## Install

1. Download `TeleVip-…-debug.apk` from the [latest release](../../releases/latest) and install it.
2. In LSPosed / Vector: **Modules** → enable **TeleVip** → tick your Telegram clients.
3. **Force stop** the client and open it again.

## Supported clients

Telegram, Telegram Beta and Web, Plus Messenger, Nagram, NagramX, Nagram XF, **Nekogram**,
Nekogram X, **Cherrygram**, Nicegram, iMe, X Plus, ForkClient, Forkgram, Skygram, Teegra,
Telegraph, Telega, Momogram, Turrit and TG Connect.

Obfuscated forks such as Nekogram and Cherrygram rename their code on every release. TeleVip
doesn't depend on a name table for a single version. It reads the running client's own APK once
per update and finds each hook by what the code does. Anything it can't pin down exactly is left
off rather than guessed. Details are in the [changelog](CHANGELOG.md).

## Building

```bash
./gradlew :app:assembleDebug
```

JDK 17 and Android SDK 36. Pushing to `main` builds APKs in [Actions](../../actions/workflows/build-apk.yml).
Bumping `versionName` on `main` publishes a release.

## Credits

- **[Mustafa (@mustafa1dev)](https://github.com/mustafa1dev)** is the original author of TeleVip
  ([mustafa1dev/TeleVip-LSPosed](https://github.com/mustafa1dev/TeleVip-LSPosed)).
- Partly based on [Re-Telegram](https://github.com/Sakion-Team/Re-Telegram).

## License

[GPL-3.0](LICENSE). This project is for educational use. Modified clients can put a Telegram
account at risk, so use it at your own risk.
