<div align="center">

<img src=".github/assets/logo.svg" width="128" alt="TeleVip logo">

# Re: TeleVIP

**Privacy, Ad-Blocking and quality-of-life Features for Telegram and its forks, as a Vector/Xposed module.**

[![Release](https://img.shields.io/github/v/release/2B-4G10/Re-TeleVIP?label=release&color=FFD500)](../../releases/latest)
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
| Block ads: sponsored posts, video and search ads, proxy sponsor | Hijri and Persian dates | Hide app update prompts |

…and more in the module's settings, which live inside the client's own **Settings**.

## Requirements

- **LSPosed 1.10+** or **Vector 2.2+** (libxposed API 102). LSPosed 1.9.x, EdXposed and LSPatch are not supported.
- Any Zygisk provider: Magisk, KernelSU, Zygisk Next or NeoZygisk.
- Android 8.1 or newer.

## Install

1. Download `TeleVip-…-release.apk` from the [latest release](../../releases/latest) and install it.
2. In LSPosed / Vector: **Modules** → enable **TeleVip** → tick your Telegram clients.
3. **Force stop** the client and open it again.

## Supported clients

### ✅ Checked every week

Every Monday, each feature's hook points are checked against the latest releases of these
clients. A release that breaks something opens an issue.

<table>
<tr>
  <td align="center" width="25%"><img src=".github/assets/clients/telegram.png" width="56" height="56" alt=""><br><b>Telegram</b><br><sub>telegram.org · newest</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/nekogram.png" width="56" height="56" alt=""><br><b>Nekogram</b><br><sub>GitHub · last 5</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/cherrygram.png" width="56" height="56" alt=""><br><b>Cherrygram</b><br><sub>GitHub · last 5</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/nagram.png" width="56" height="56" alt=""><br><b>Nagram</b><br><sub>GitHub · last 5</sub></td>
</tr>
<tr>
  <td align="center" width="25%"><img src=".github/assets/clients/nagramx.png" width="56" height="56" alt=""><br><b>NagramX</b><br><sub>GitHub · last 5</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/forkgram.png" width="56" height="56" alt=""><br><b>Forkgram</b><br><sub>F-Droid · last 5</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/forkgram-classic.png" width="56" height="56" alt=""><br><b>Forkgram Classic</b><br><sub>F-Droid · last 5</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/mercurygram.png" width="56" height="56" alt=""><br><b>Mercurygram</b><br><sub>F-Droid · last 5</sub></td>
</tr>
</table>

### ☑️ Supported, not checked automatically

The same Telegram code base, but published where the weekly check can't download it. To check
one, run the *Client watch* workflow by hand with a link to its APK.

<table>
<tr>
  <td align="center" width="20%"><img src=".github/assets/clients/telegram.png" width="56" height="56" alt=""><br><b>Telegram</b><br><sub>Google Play & Beta</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/plus.png" width="56" height="56" alt=""><br><b>Plus Messenger</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/nicegram.png" width="56" height="56" alt=""><br><b>Nicegram</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/ime.png" width="56" height="56" alt=""><br><b>iMe</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/xplus.png" width="56" height="56" alt=""><br><b>X Plus</b><br><sub>Google Play</sub></td>
</tr>
<tr>
  <td align="center" width="20%"><img src=".github/assets/clients/turrit.png" width="56" height="56" alt=""><br><b>Turrit</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/telegraph.png" width="56" height="56" alt=""><br><b>Telegraph</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/tgconnect.png" width="56" height="56" alt=""><br><b>TG Connect</b><br><sub>Google Play</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/telega.png" width="56" height="56" alt=""><br><b>Telega</b><br><sub>RuStore</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/momogram.png" width="56" height="56" alt=""><br><b>Momogram</b><br><sub>community fork</sub></td>
</tr>
<tr>
  <td align="center" width="20%"><img src=".github/assets/clients/nagramx.png" width="56" height="56" alt=""><br><b>Nagram XF</b><br><sub>NagramX variant</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/forkgram.png" width="56" height="56" alt=""><br><b>ForkClient</b><br><sub>Forkgram beta</sub></td>
  <td align="center" width="20%"><img src=".github/assets/clients/nekox.png" width="56" height="56" alt=""><br><b>Nekogram X</b><br><sub>discontinued</sub></td>
</tr>
</table>

### 🟡 Partly works

<table>
<tr>
  <td align="center" width="25%"><img src=".github/assets/clients/telegram-foss.png" width="56" height="56" alt=""><br><b>Telegram FOSS</b><br><sub>F-Droid · 10.14.3 (2024)</sub></td>
  <td>Its last release predates the Telegram code behind <i>Block ads</i>, <i>Save edits history</i>, Ghost Mode's paid reactions and a photo-viewer button. Everything else works.</td>
</tr>
</table>

### ⛔ Not supported

TeleVip hooks Android builds of Telegram's own app. These are separate apps, or not Android:

<table>
<tr>
  <td align="center" width="25%"><img src=".github/assets/clients/telegram-x.png" width="56" height="56" alt=""><br><b>Telegram X</b><br><sub>a different app (TDLib)</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/telegram-ios.png" width="56" height="56" alt=""><br><b>Telegram for iOS</b><br><sub>iPhone & iPad</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/tdesktop.png" width="56" height="56" alt=""><br><b>Telegram Desktop</b><br><sub>Windows · macOS · Linux</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/unigram.png" width="56" height="56" alt=""><br><b>Unigram</b><br><sub>Windows</sub></td>
</tr>
<tr>
  <td align="center" width="25%"><img src=".github/assets/clients/ayugram.png" width="56" height="56" alt=""><br><b>AyuGram</b><br><sub>desktop</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/64gram.png" width="56" height="56" alt=""><br><b>64Gram</b><br><sub>desktop</sub></td>
  <td align="center" width="25%"><img src=".github/assets/clients/kotatogram.png" width="56" height="56" alt=""><br><b>Kotatogram</b><br><sub>desktop</sub></td>
</tr>
</table>

<sub>App icons belong to their respective projects and are shown only to identify them.</sub>

### How it keeps up

Telegram and its forks rename their code on every release, some (Nekogram, Cherrygram) almost
all of it. TeleVip doesn't depend on a name table for a single version. It reads the running
client's own APK once per update and finds each hook by what the code does. Anything it can't pin
down exactly is left off rather than guessed. Details are in the [changelog](CHANGELOG.md).

## Credits

- **[Mustafa (@mustafa1dev)](https://github.com/mustafa1dev)** is the original author of TeleVip
  ([mustafa1dev/TeleVip-LSPosed](https://github.com/mustafa1dev/TeleVip-LSPosed)).
- Partly based on [Re-Telegram](https://github.com/Sakion-Team/Re-Telegram).

## License

[GPL-3.0](LICENSE). This project is for educational use. Modified clients can put a Telegram
account at risk, so use it at your own risk.
