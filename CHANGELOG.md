# Changelog

## Unreleased — privacy and security

- **Ghost Mode tells you when it can't work.** If an app update moves what Ghost Mode relies
  on, Re: TeleVIP now says which options are affected, when the app starts and when you open
  its settings, instead of leaving the switches on while read receipts, typing or online status
  are sent as normal.
- **Ghost Mode no longer lets an occasional request slip through.** With Mark read after send
  on, a typing, online or read request sent at the same moment could go out unhidden.
- **Edit histories stay with their own message.** A message could show "edits" that came from
  another chat, or from another of your accounts. Histories are now kept per account and per
  chat. Histories saved by older versions are cleared once, since they can't be sorted out.
- **Clear saved data.** A new button under Messages deletes the edit histories and the list of
  kept self-destructing media stored on your phone.
- **Saving secret media is more careful.** Re: TeleVIP tells you which folder it saved to, and
  that other apps with photo access can see it there. On versions where it can't tell which
  file the media is, it now saves nothing rather than risk saving a different chat's media. A
  save that fails no longer leaves an empty item in the gallery.
- **Safer releases.** Every build step is pinned and checked, and the signing key is only
  handed to the step that signs the release, so a compromised build tool can't take it.
  Releases now come with the signed APK only. Two unused libraries were removed from the
  module.

## 1.3 — Graph Messenger, save secret media, and chats that load with Ghost Mode

- **You decide when the sender sees your secret media as opened.** With Prevent media on,
  opening a self-destructing photo or video tells the sender nothing. Tap **Mark as seen** in
  the viewer when you want them to see it as opened; their copy then expires as usual, and you
  keep yours.
- **Save secret media from the viewer.** A **Save** button puts the photo or video in your
  gallery, next to **Mark as seen** and **Delete for both**.
- **Graph Messenger is supported.** Re: TeleVIP shows up in its settings and its features work
  on Graph's current versions (12.10 and 13.0), which are checked every week like the other
  clients.
- **Hide seen works smoothly on Nekogram 12.10.5.** Reading a chat with Ghost Mode on could
  run into an error there, which got in the way of chats loading. Chats now open normally, and
  the sender still doesn't see that you read them.
- **Save edits history works on Nekogram and Cherrygram.** On these clients it couldn't tell
  which chat an edited message belonged to, so earlier versions weren't kept. The secret media
  viewer had the same trouble there and now finds the right chat too.
- **Re: TeleVIP settings open every time.** After leaving the app with Back and opening it
  again, tapping the settings did nothing until the app was force stopped.

## 1.2.2 — Nekogram 12.10.5, fair secret media, and smoother settings

- **Prevent media and Save secret media work on Nekogram 12.10.5.** Nekogram renamed part of
  the chat in that version, so tapping a photo no longer reached Re: TeleVIP. Newer Nekogram
  versions keep working as before.
- **The sender sees your secret photos and videos as opened.** With Prevent media on, you
  still keep your copy, but the sender's copy now expires as usual.
- **"Delete for both" in the secret media viewer.** After you confirm, the photo or video is
  deleted for you and the sender, as the timer would have done.
- **No more photos downloading over and over** in groups while Save secret media is on.
- **Night mode and theme colours work right after closing Re: TeleVIP settings.** Until now
  they did nothing until you restarted the app.
- **Dialogs follow light and dark mode.** No more white text on a white background in the
  calendar and other Re: TeleVIP dialogs.
- **Show deleted messages and Voice messages as music turn off right away**, without a
  restart. Save edits history and Show deleted messages are also gentler on the chat database.

## 1.2.1 — no false "incompatible version" warning on Nekogram

- **Fixed: Nekogram showed "Your Telegram client may be an incompatible version"** in the log
  with Prevent media on, after 1.2. Nothing was broken, and the warning is gone.

## 1.2 — Ghost Mode fixed, tested on more phones, and no root needed

- **Ghost Mode works again with Block ads on.** With both switched on, Ghost Mode quietly did
  nothing: chats were marked as read and others could see you typing and online. Now your
  "seen", typing and online status stay hidden.
- **Self-destructing photos stay.** On Telegram 13.1, a timed photo still expired after you
  opened it. It now stays, as "Prevent media" promises.
- **Works without root through LSPatch 1.2.** Patch your Telegram client with JingMatrix's
  LSPatch, in Local or Integrated mode, and add Re: TeleVIP to it. The README has the steps.
- **Plus Messenger is supported**, and checked every week like the other clients.
- **Telegram 13.1 and 13.0.3 (Play Store) are supported.** The error about the secret media
  viewer is gone.
- **Save edits history works in Nagram 1253 and Momogram 13**, where it found nothing to hook.
- **Forkgram Classic 13 shows the Re: TeleVIP entry in its side menu again.**
- **No more crash from the edits-history storage** ("Can't downgrade database from version 2
  to 1") after switching from another TeleVip build. Your saved edits are kept.
- **Hide phone number shows "Number hidden"** instead of "+null" in Settings.
- **Hide app update also hides the update pop-up** that Nekogram shows when you open the app.
- **Long setting titles wrap neatly** and no longer cover the line under them, in every
  language.
- **Checked on Android 13 to 16** with Magisk, KernelSU, KernelSU Next, BakaSU and APatch,
  and Vector, in all 12 supported clients. The README shows which ones were tried on each
  Android version.

## 1.1.10 — a clearer settings screen

- **Redesigned settings.** Each group (Ghost Mode, Stories, Messages, Connections, Media, UI and
  Other Features) is now its own card with an icon, and every option has a short line under it
  saying what it does, in English, Arabic, Persian and Chinese. "Restart required" sits on its own
  line. Your saved settings are kept.
- **Join the Re: TeleVIP group.** The developer channel button is replaced by the new group,
  [t.me/Revived_TeleVIP](https://t.me/Revived_TeleVIP), shown with the app's logo. The join prompt points
  there too.

## 1.1.9 — no more broken hooks on Nagram, Nekogram and Nagram X

- **Fixed: Block Ads' second barrier did nothing on Nagram and Nekogram.** When an ad request
  got through anyway, the module could not build the "no ads" answer to put in place of the
  server's, because those builds strip its constructor (they never create it themselves). It is
  now made without one, with its lists empty as usual.
- **The hook health line no longer reports code a client removed on purpose as missing.** Nagram
  X drops search ads (`contacts.getSponsoredPeers`), some Nagram builds drop
  `MessagesController.getSponsoredMessages` and Nekogram drops the settings row's icon cell; the
  module already covered each another way, but logged them as "missing" and "Your Telegram client
  may be an incompatible version". They are now listed as "not in this build (fallback in use)",
  with the reason, and a class that is not found is logged and counted once.
- The list of what a build may lack now lives in the module itself, so the weekly client check
  and the module's own report always agree. Checked on Telegram 13.0.3, Nekogram 13.0.2,
  Cherrygram 11.3.0, Nagram 1253, Nagram X 1260 and Momogram 12.10.6: every hook resolves.

## 1.1.8 — Nekogram 13 and Nagram 1253

- **Fixed: Re: TeleVIP on Nekogram 13.0.2**, the first build on Telegram 13.0: secret media
  saving, keeping messages forwardable, the voice-to-music switch and reading secret messages
  could not find their code. Telegram 13 adds a table of every message type that misled the
  module's lookup; it now skips it.
- **Fixed: Re: TeleVIP on Nagram 1253** (12.10.6): its colours (dark theme check, message time
  paint) were not found because Nagram now renames the theme class.

## 1.1.7 — weekly update

- Build: Bump gradle-wrapper from 9.8.0 to 9.8.1 in the gradle group (#5)

## 1.1.6 — the settings screen on every client, and Momogram

- **Fixed: Re: TeleVIP Settings could open empty**, showing only its title bar (reported on
  Nagram X 12.9.2). The list was built on the client's own RecyclerView, which forks rename,
  strip or change. It is now a plain scrolling list of the same rows, built directly, so it
  depends on nothing the client may have changed; a row the client cannot build is left out
  instead of blanking the whole screen. This also removes the separate settings adapter that
  was injected into the client.
- **Momogram 12.10.x: every feature works.** Momogram renames and reorders much of its code,
  and looks its strings up by resource id, so Save secret media, Show deleted messages, edits
  history and more found nothing to hook. Hide app update is no longer offered there: Momogram
  updates itself through its own updater.
- **Nagram X's "base" build** (`nu.gpu.nagramx`) is supported.
- The weekly client check now also covers Momogram and Nagram X base: ten clients, the last
  five releases of each.

## 1.1.5 — the app is called Re: TeleVIP

- **The module is now named "Re: TeleVIP"** in LSPosed / Vector and in Android's app list (it
  was "TeleVip"), and as the title of the account-age dialog in profile menus. The README, here
  and in the Xposed Modules Repo, uses the new name too. The package name and the APK's file
  name are unchanged.

## 1.1.4 — Re: TeleVIP Settings

- **TeleVip's entry in Settings is now called "Re: TeleVIP Settings 🛸"**, with "Made by:
  @i_2B_4G10" below it. The settings screen it opens, and TeleVip's dialogs, use the new name
  too, in all four languages (English, Arabic, Persian, Chinese).

## 1.1.3 — TeleVip's own icon in Settings

- **TeleVip's entry in Settings shows TeleVip's app icon** — the paper plane on its yellow tile —
  instead of the ghost.
- Also from 1.1.2: Cherrygram 11.3.0, which is built on Telegram 12.10.6, had the same problem as
  Nekogram 12.10.6 and works again too.

## 1.1.2 — Nekogram 12.10.6 works again

- **Fixed: on Nekogram 12.10.6 TeleVip did almost nothing** — no TeleVip entry in Settings, ads
  still shown, and most other features off. Since 1.0.8, TeleVip had mistaken that build for one
  covered by an old name table made from Nekogram 12.8.1, so it looked for classes that do not
  exist in 12.10.6. TeleVip now always reads the names from the installed app, on every client
  and build, which is what the weekly client checks test. Cherrygram's table was mislabelled the
  same way.
- **TeleVip's entry is back at the top of Settings, with its ghost icon.** On builds that rename
  the row's icon view beyond recognition, TeleVip now finds the icon by itself.

## 1.1.1 — ads blocked at every step

- **Block Ads now has three barriers instead of one**, so a channel's sponsored post cannot get
  through if one of them misses:
  1. the request that fetches ads is dropped before it is sent, as before;
  2. if an ad request is sent anyway, the server's answer is read as "no ads";
  3. a chat that asks for its sponsored posts is told there are none, whatever was fetched or
     cached.

  This covers sponsored posts in channels and bot chats, the video player's ads and the "Ad"
  results in chat search. The pinned proxy sponsor channel stays removed as before.
- **Block Ads is on by default.** If you never touched the switch, it is now on; if you turned
  it off, it stays off.

## 1.1.0 — new package name, for the Xposed Modules Repo

- **TeleVip's package name is now `io.github.re_televip.televip`** (it was `com.my.televip`).
  The Xposed Modules Repo (modules.lsposed.org) only lists modules whose package name their
  author owns, and this one belongs to the Re-TeleVIP organisation. Android treats it as a new
  app, so once:
  1. Install this version, then uninstall the old *TeleVip* (`com.my.televip`).
  2. In LSPosed / Vector, enable TeleVip again and tick your Telegram clients.
  3. Force stop the clients and reopen them.

  Your TeleVip settings are kept: they are stored inside each Telegram client, not in TeleVip.
- Releases are also published to TeleVip's page in the Xposed Modules Repo, so LSPosed's module
  repository can offer updates.

## 1.0.8 — Show deleted messages on Nekogram 12.10.5+, Android 17 SDK

- **Show deleted messages works on Nekogram 12.10.5 and 12.10.6.** 1.0.7 picked the wrong event
  there: Nekogram numbers its events one higher than Telegram does. It also intercepted a
  profile-screen event instead. The event is now read from the code that posts it, never
  assumed. That is correct on all 37 builds checked, including Telegram FOSS 10.14.3, whose
  events are numbered differently again.
- Built against the Android 17 SDK (API 37). The weekly toolchain update now keeps the SDK
  current too, alongside AGP, Gradle and the libxposed API, and pushes only if the build and
  tests pass.
- Client watch: the F-Droid listing no longer fails on clients with a long history (Forkgram).
- Clean-up: unused resolver code removed, compiler warnings fixed, and deprecated Android calls
  replaced (the client's version code; the audio player's stream type).
- The builds TeleVip was last verified against are updated: Telegram Web 12.10.6, Nekogram
  12.10.6, Cherrygram 11.3.0, Nagram 1248, NagramX 1258, Forkgram 12.10.4 and Forkgram Classic
  12.10.8.

## 1.0.7 — Nekogram 12.10.5+ and the last five releases of every client

- **Nekogram 12.10.5 and 12.10.6 are supported.** Their build renames, reorders, merges and
  inlines much more than before, which broke the settings entry, Ghost Mode, Block Ads,
  saving edits, the photo viewer and more. Every feature now finds its code there again.
- **Mercurygram** is supported.
- **Every feature was checked on the last five releases of each client** that can be
  downloaded, 36 builds in all:
  - Nekogram 12.9.2–12.10.6, Cherrygram 10.9–11.3, Nagram and NagramX;
  - Forkgram 12.9.5–12.10.4, Forkgram Classic 12.10.3–12.10.8 and Mercurygram 12.9.0–12.10.5;
  - Telegram 12.10.5 and 12.10.6.
- **The weekly Client watch now checks the last five releases of each of these clients**, not
  just the newest one. Forkgram, Forkgram Classic and Mercurygram are fetched from F-Droid.
- *Ghost Mode*: reading a channel without being seen marked it read with the wrong id on Nagram.
  The account's own channel lookup is used now.
- *Fix TL error*: the parse error is recognised by its message, where the build has no name
  for it.
- *Disable number rounding*, *Save edits history* and *Hide app updates* keep working where the
  build reorders those methods' parameters or drops their return value.
- TeleVip's dialogs use the system dialog everywhere. The Telegram-styled one never actually
  loaded, because of a misspelt class name.

## 1.0.6 — ad blocking that works

- **Hide Proxy Sponsor is now Block Ads**, and your earlier setting carries over. It blocks the
  requests that fetch ads before they are sent, so nothing is downloaded and nothing is shown:
  - sponsored posts in channels and bot chats, and the ads in the video player;
  - the "Ad" results in chat search;
  - the proxy sponsor / announcement channel pinned to the top of the chat list.
- **The sponsor channel no longer comes back.** It used to be removed only after the client had
  already asked the server for it, so the reply put it straight back. The check is now skipped,
  and a channel saved from before is removed along with the settings that restored it on start.
- The ad requests are found by their protocol ids on renamed builds. Every Block Ads hook point
  resolves on Telegram 12.10.5, Nekogram 12.10.3 and Cherrygram 12.10.1, and the weekly Client
  watch checks them.

## 1.0.5 — every feature reaches its code

Every feature's lookups were replayed against Telegram 12.10.5, Nekogram 12.10.3 and
Cherrygram 12.10.1, and the ones that pointed at nothing were fixed:

- **TeleVip's entry in Settings is back on official Telegram**, which made the methods it hooks
  static and dropped their unused parameters.
- **TeleVip's settings page no longer fails on Telegram and Cherrygram**: the switch rows it is
  built from are found there again.
- **Nicegram, Nagram, Turrit, Momogram and NekoX**: the parameter lists these forks changed are
  honoured again (1.0.4 ignored them, which silently disabled some hooks there).
- *Jump to message* works where Telegram inlined the overload it called; *Hide seen* finds
  the channel lookup on renamed builds.
- Found on more builds: the photo viewer, a viewer's image, the local file path of media and the
  settings adapter.
- Fingerprints hold on Nagram and NagramX too, which add their own menu items and look-alike cells.
- Weekly automation: Dependabot updates are merged once they build, and the Monday Client watch
  releases whatever changed if every client still checks out.
- Removed the fourteen empty per-client name tables.

## 1.0.4 — official Telegram and Cherrygram

- **Official Telegram, Forkgram and the other unobfuscated forks now get the same runtime
  resolution as Nekogram.** Telegram renames its UI classes (ChatActivity, SettingsActivity,
  PhotoViewer's menus, ...) in every release, and TeleVip looked them up by their source names
  there, so the features that hook them stayed off. Every client is now resolved from its own
  APK, keeping a hand-made name table only on the exact build it was made for. Every feature hook
  point resolves on Telegram 12.10.5.
- **Cherrygram 12.10.1 is supported again.** It renames even the core `org.telegram.messenger`
  and TL classes and nearly all of their methods. These are now found by strings only they load,
  TL constructor ids, and the names that native code forces every build to keep. Every feature
  hook point resolves on it.
- Fingerprints follow more of R8's rewrites: methods turned static, narrowed field and return
  types, and fields that are no longer narrowed.
- The weekly Client watch also checks that no fingerprint ever picks a different symbol than the
  real name, wherever a build keeps it.

## 1.0.3 — release safeguard

- A release is never published without the signed APK: if the signing secrets are missing or
  stop working, the release fails instead of shipping only the debug build.
- Installs over 1.0.2 directly (same signing key).

## 1.0.2 — signed release APK

- **The release APK is now signed**, so it installs directly. Builds read the key from the
  `KEYSTORE_*` repository secrets (or a local `keystore.properties`); nothing secret is kept in
  the repository. Because it is signed with a different key than the debug APK, uninstall a debug
  build once before installing the release one.
- Hooking several methods at once no longer skips the rest when one of them is missing.
- Mapping caches of earlier client builds are deleted when a new one is written.
- Removed unused code, resources and template files; Android Gradle Plugin 9.4.1.

## 1.0.1 — works on renamed builds, Ghost Mode dialog fixed

Version numbering restarts at 1.0.1 (versionCode 341, so it still installs over 3.7.0).

### Fixed

- **Tapping *Join* in the Ghost Mode dialog crashed Nekogram, and *Dismiss* left the Ghost Mode
  button dead.** On Nekogram 12.10.3 `LaunchActivity.frameLayout` resolved to the tablet-only
  `shadowTablet`, which is null on phones: showing the page failed and hiding it threw from
  inside the dialog's click handler. The field is now found as the view `onCreate` hands to
  `setContentView`, the wrapper checks it really is the window's content view (and falls back to
  that view when it is not), `show`/`hide` can no longer throw, and dialog button actions run
  inside a guard so a failure is logged instead of taking the client down.
- The dialog button listener no longer depends on the listener method's name, and answers
  `equals`/`hashCode` properly instead of returning null.

### Restored on obfuscated (R8-renamed) builds

Every feature below was inactive on Nekogram 12.10.3 because its hook point was renamed,
inlined, reordered or narrowed. Each is now found by what the code does, checked against the
12.10.3 bytecode in `NekogramApkTest`:

- **Hide pinned messages** — `updatePinnedMessageView` (R8 swapped its parameters to
  `(int, boolean)`; the real order is now published to the hook), `createPinnedMessageView` and
  the `pinnedMessageView` field.
- **Remove content-saving restrictions** — `hasSelectedNoforwardsMessage`.
- **Save edits history** — `fillMessageMenu`, `processSelectedOption`, `selectedObject`.
- **Show deleted messages** — `measureTime` and the time-label fields it writes
  (`currentTimeString`, `timeWidth`, `timeTextWidth`, `Theme.chat_timePaint`).
- **Secret media save** — the chat's `didPressImage` (R8 dropped its unused flag).
- **Save protected stories** — `StoryItemHolder.allowScreenshots`.
- **Disable stories** — `hasStories(long)` is hooked on every client, since `hasStories()` is
  inlined away in some builds.
- **Always save media** — `setIsAboutToSwitchToIndex` and `galleryButton`; `openPhoto` and
  `setParentActivity` fall back to the one overload R8 keeps.
- **Chat and profile menu entries** (*To the beginning*, *To the message*, *Approximate creation
  date*) — `ActionBarMenuItem` and its `addSubItem`/`lazilyAddSubItem`, `headerItem`,
  `otherItem`, `createActionBarMenu`. Clicks are now routed from each screen's live menu listener
  instead of by class name, which renamed builds do not keep.
- **Show user ID / hide online status on profiles** — `updateProfileData`, `userId`, `chatId`,
  `nameTextView`, `onlineTextView`, and `SimpleTextView`'s text methods (found by shape when
  renamed).
- Menu and settings icons fall back to the resource table when `R$drawable` has been stripped.

The resolver gained the matching tools: parameter-order tolerance, "called by" and
"stores a new anonymous subclass" facts, subtype-narrowed fields, and class lookup by one
distinctive method. The fingerprint version is bumped, so a cached mapping from the previous
module version is discarded and rebuilt on first start.

### Hook resilience

- **Signature drift no longer kills a hook.** When the exact signature is gone the method is
  re-matched by name, but only when the match is unambiguous (same arity when parameter types are
  given); anything else is left unhooked rather than attached to the wrong overload.
- **A parameter type that no longer resolves is a wildcard** instead of failing the whole hook.
- **`HookHealth` logs one line at startup**: how many hooks resolved, drifted or are missing.

## 3.7.0 — Xposed API 102, Vector 2.2, Zygisk Next, Nekogram X

### Modern Xposed API (libxposed 102)

TeleVip is now a **libxposed API 102 module**, loaded through a single modern entry point:

| Entry | Descriptor | Class |
|---|---|---|
| Modern (API 102) | `META-INF/xposed/java_init.list` + `module.prop` | `com.my.televip.xposed.TeleVipModule` |

It funnels into `com.my.televip.xposed.ModuleEntry#attach`. The legacy API 93 entry
(`assets/xposed_init` → `MainHook`) was dropped before release: leaving it in place would make
LSPosed 1.9.x and EdXposed advertise the module and then fail at load time, because there is no
longer a `de.robv` entry class for them to find.

New compatibility layer under `com.my.televip.xposed`:

- **`XBridge`** — the only seam to the framework: hooking, logging, module APK path, deoptimize,
  framework name/version. Everything else in the module goes through it.
- **`ModernBackend`** — adapts to `XposedInterface.hook(Executable).intercept(Hooker)`. API 102
  replaced the before/after pair with an OkHttp-style interceptor chain, so the adapter rebuilds
  classic semantics on top of `Chain`: a result set in `beforeMethod` short-circuits the chain
  (original never runs), otherwise `chain.proceed(args)` runs with the mutated argument array and
  its outcome is handed to `afterMethod`, which may still override it.

`AbstractMethodHook` no longer extends `XC_MethodHook`; it is a plain class with a nested
`MethodHookParam` that keeps the same surface (`args`, `thisObject`, `getResult`, `setResult`,
`setThrowable`), so all existing feature code compiles unchanged. `XC_MethodReplacement` is
replaced by `com.my.televip.base.MethodReplacement`.

### No runtime dependency on `de.robv.*` for reflection

A module loaded through the modern API gets **no legacy Xposed classes at all**, so every
`XposedHelpers` call would have thrown `NoClassDefFoundError`. All 58 call sites now use
`com.my.televip.reflect.XReflect`, a self-contained reimplementation with the same signatures,
the same best-match argument resolution (exact match → boxing → primitive widening →
`null`-compatible, most specific wins) and the same unchecked `NoSuchMethodError` /
`NoSuchFieldError` behaviour. Field and class lookups are cached.

### Vector 2.2 / Zygisk Next hardening

- The module APK path no longer depends on `initZygote()`, which the modern API does not have and
  which is unreliable for app-scoped modules under Zygisk Next. `XBridge#modulePath` takes it from
  the backend (`getModuleApplicationInfo().sourceDir` on modern, `StartupParam` on legacy) and
  falls back to deriving it from our own class loader.
- Language packs moved from `assets/lang/` to `src/main/resources/lang/` and are read straight off
  the module class loader — no APK path needed at all. The old ZIP scan is kept as a fallback and
  still auto-discovers packs the index does not list.
- `Logger` routes through `XBridge` and degrades to `android.util.Log` if no backend is
  installed yet.
- Startup now logs the active backend, framework name and version.

### Nekogram

- **Nekogram X added** (`nekox.messenger`): new `Clients/NekogramX.java` resolver, `ClientType`
  entry and `META-INF/xposed/scope.list` item. Like its Momogram fork, NekoX ships unobfuscated
  Telegram symbols, so the mapping tables are empty and names resolve to upstream Telegram names.
- **Version drift is now visible.** `ClientChecker#checkClientVersion` records the build each
  resolver table was generated against and logs one clear warning at startup when the installed
  client differs — loud for the obfuscated clients (Nekogram, Cherrygram), where a mismatch means
  essentially every hook silently fails.
- **Official Telegram re-verified against 12.10.1** (build 70382). The symbols the module resolves
  by name — `TLRPC.Message` fields, `TL_messages_readHistory` / `TL_channels_readHistory`,
  `TL_updateDeleteMessages`, `MessagesController` / `ConnectionsManager` / `UserConfig` accessors
  and `PeerStoriesView$StoryItemHolder#allowScreenshots` — are unchanged from the previously
  verified build, so no hook needed updating.

> The Nekogram/Cherrygram R8 mapping tables themselves still have to be regenerated from the target
> APK whenever those clients update; that cannot be done from source.

### Build / repo fixes

- `settings.gradle` included `':TeleVip'`, a module that does not exist — removed.
- `.gitignore` had an unresolved merge conflict (`<<<<<<< HEAD` … `>>>>>>>`) committed to it —
  resolved, plus the generated `DexHolder.java` is now ignored.
- Added `io.github.libxposed:api:102.0.0` as a `compileOnly` dependency in the version catalog.
- ProGuard rules for both entry points (`-adaptresourcefilecontents META-INF/xposed/java_init.list`
  and the `XposedModule` keep rule).
- `xposeddescription` moved to a string resource, with `android:description` added for the modern
  API (which reads the module description from there); Arabic kept in `values-ar`.
- Version bumped to 3.7.0 (340).
