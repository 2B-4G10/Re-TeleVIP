package com.my.televip.updates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.my.televip.BuildConfig;
import com.my.televip.Configs.ConfigPreferences;
import com.my.televip.application.AndroidUtilities;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.settings.controller.SettingsController;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.messenger.browser.Browser;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * Tells the user when a newer Re: TeleVIP is out on GitHub. The only network call TeleVip makes
 * of its own: one HTTPS GET of the latest release from api.github.com, at most once a day, off the
 * main thread, with nothing about the user or the phone in it. Any failure is dropped silently.
 * The check reads the GitHub release, the source of truth; the Xposed Modules repo mirrors it.
 */
public final class UpdateCheck {

    private static final String LATEST = "https://api.github.com/repos/2B-4G10/Re-TeleVIP/releases/latest";

    private static final long DAY = 24L * 60 * 60 * 1000;
    /** Wait for the client to settle, and come after the Ghost Mode warning. */
    private static final long START_DELAY = 30_000;
    private static final int TIMEOUT = 15_000;
    /** A release's JSON is a few KB; anything far past that is not what we asked for. */
    private static final int MAX_BYTES = 512 * 1024;

    private static final String LAST_CHECK = "UpdateCheckLast";
    private static final String SKIPPED = "UpdateCheckSkipped";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "TeleVip-UpdateCheck");
        thread.setDaemon(true);
        return thread;
    });

    private UpdateCheck() {
    }

    public static String installedVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /** At start: checks when the switch is on and the last check was a day or more ago. */
    public static void init(SettingsController settingsController) {
        AndroidUtilities.runOnUIThread(() -> {
            try {
                if (!ConfigPreferences.getBoolean(Keys.CheckForUpdates, true)) return;
                long last = ConfigPreferences.getLong(LAST_CHECK);
                long now = System.currentTimeMillis();
                // A clock set back would otherwise hold the next check off for good.
                if (now - last < DAY && now >= last) return;
                ConfigPreferences.putLong(LAST_CHECK, now);
                fetch(release -> {
                    if (release == null) return;
                    if (!Release.isNewer(release.version, installedVersion())) return;
                    if (release.tag.equals(ConfigPreferences.getString(SKIPPED))) return;
                    show(settingsController.getContext(), release, true);
                });
            } catch (Throwable t) {
                Logger.e(t);
            }
        }, START_DELAY);
    }

    /** "Check for updates" in the settings: checks now, and says so when there is nothing new. */
    public static void checkNow(SettingsController settingsController) {
        try {
            Toast.makeText(settingsController.getContext(), Translator.get(Keys.UpdateChecking), Toast.LENGTH_SHORT).show();
            fetch(release -> {
                Context context = settingsController.getContext();
                if (release == null) {
                    Toast.makeText(context, Translator.get(Keys.UpdateCheckFailed), Toast.LENGTH_SHORT).show();
                } else if (Release.isNewer(release.version, installedVersion())) {
                    ConfigPreferences.remove(SKIPPED);
                    show(context, release, false);
                } else {
                    Toast.makeText(context, Translator.get(Keys.UpdateUpToDate, installedVersion()), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    private interface Result {
        void onResult(Release release);
    }

    /** Reads the latest release off the main thread and hands it over on it; null on failure. */
    private static void fetch(Result result) {
        EXECUTOR.execute(() -> {
            Release release = null;
            try {
                release = latest();
            } catch (Throwable t) {
                Logger.w("Update check failed: " + t);
            }
            Release found = release;
            AndroidUtilities.runOnUIThread(() -> {
                try {
                    result.onResult(found);
                } catch (Throwable t) {
                    Logger.e(t);
                }
            });
        });
    }

    private static Release latest() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(LATEST).openConnection();
        try {
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            // GitHub turns away requests without one; the same for everyone, so it says nothing.
            connection.setRequestProperty("User-Agent", "Re-TeleVIP");
            if (connection.getResponseCode() != 200) return null;
            JSONObject json = new JSONObject(read(connection.getInputStream()));
            if (json.optBoolean("draft") || json.optBoolean("prerelease")) return null;
            String tag = json.optString("tag_name", "");
            String url = json.optString("html_url", "");
            // Only ever send the user to this repository's releases.
            if (tag.isEmpty() || !url.startsWith("https://github.com/2B-4G10/")) return null;
            return new Release(tag, url, json.optString("body", ""));
        } finally {
            connection.disconnect();
        }
    }

    private static String read(InputStream in) throws Exception {
        try (InputStream stream = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = stream.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                if (out.size() > MAX_BYTES) throw new IllegalStateException("response too large");
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** The update prompt: what's new, Download (the release page), Later and Skip this version. */
    private static void show(Context context, Release release, boolean offerSkip) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        AlertDialog dialog = new AlertDialog(activity);
        dialog.setTitle(Translator.get(Keys.UpdateAvailable, release.version));
        String installed = Translator.get(Keys.UpdateInstalled, installedVersion());
        dialog.setMessage(release.notes.isEmpty() ? installed : installed + "\n\n" + release.notes);
        dialog.setPositiveButton(Translator.get(Keys.UpdateDownload), AlertDialog.click(() -> open(activity, release.url)));
        dialog.setNegativeButton(Translator.get(Keys.UpdateLater), null);
        if (offerSkip) {
            dialog.setNeutralButton(Translator.get(Keys.UpdateSkip), AlertDialog.click(() -> ConfigPreferences.putString(SKIPPED, release.tag)));
        }
        Translator.withTextDirection(dialog::show);
    }

    /** In the phone's browser, where the APK downloads and installs; the client's otherwise. */
    private static void open(Activity activity, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Throwable t) {
            try {
                Browser.openUrl(activity, url);
            } catch (Throwable t2) {
                Logger.e(t2);
            }
        }
    }
}
