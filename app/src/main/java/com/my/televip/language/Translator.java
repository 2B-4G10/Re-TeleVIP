package com.my.televip.language;

import com.my.televip.Configs.ConfigPreferences;
import com.my.televip.utils.Utils;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.messenger.LocaleController;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Translator {

    private static final String LANG_DIR = "lang/";
    /** Shipped packs; the index file may add more without a code change. */
    private static final String[] BUNDLED_LANGUAGES = {"en", "fa", "ru", "ar", "zh"};

    /** The languages offered in the settings' picker, in its order. */
    public static final String[] PICKER_LANGUAGES = {"en", "fa", "ru", "ar", "zh"};

    /** Where the picked language is kept; empty or missing means the client's own language. */
    private static final String LANGUAGE_PREF = "Language";

    private static final Map<String, JSONObject> langMap = new HashMap<>();

    private static LocaleController localeController;

    public static void init() {
        try {
            loadAllLanguages();

            localeController = new LocaleController();
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    /**
     * Loads the language packs from {@code src/main/resources/lang/} through the module class
     * loader, falling back to scanning the module APK (whose path is not always readable from the
     * hooked app under Zygisk Next).
     */
    private static void loadAllLanguages() {
        int loaded = loadFromClassLoader();
        if (loaded == 0) {
            loaded = loadFromModuleApk();
        }
        if (loaded == 0) {
            Logger.w("no language pack could be loaded, falling back to raw keys");
        }
    }

    private static int loadFromClassLoader() {
        int loaded = 0;
        try {
            ClassLoader loader = Translator.class.getClassLoader();
            if (loader == null) return 0;
            for (String code : listLanguages(loader)) {
                try (InputStream is = loader.getResourceAsStream(LANG_DIR + code + ".json")) {
                    if (is == null) continue;
                    langMap.put(code, new JSONObject(readFully(is)));
                    loaded++;
                } catch (Throwable e) {
                    Logger.e(e);
                }
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
        return loaded;
    }

    private static Set<String> listLanguages(ClassLoader loader) {
        Set<String> codes = new LinkedHashSet<>();
        try (InputStream index = loader.getResourceAsStream(LANG_DIR + "languages.txt")) {
            if (index != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(index, Charset.forName("UTF-8")));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) codes.add(line);
                }
            }
        } catch (Throwable ignored) {
        }
        // Never let a missing or short index drop the shipped packs.
        for (String fallback : BUNDLED_LANGUAGES) codes.add(fallback);
        return codes;
    }

    private static int loadFromModuleApk() {
        String modulePath = Utils.modulePath;
        if (modulePath == null || modulePath.isEmpty()) return 0;

        int loaded = 0;
        try (ZipFile zipFile = new ZipFile(modulePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".json")) continue;
                if (!name.startsWith(LANG_DIR) && !name.startsWith("assets/lang/")) continue;

                String langCode = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
                try (InputStream is = zipFile.getInputStream(entry)) {
                    langMap.put(langCode, new JSONObject(readFully(is)));
                    loaded++;
                }
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
        return loaded;
    }

    private static String readFully(InputStream is) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString("UTF-8");
    }

    /** The code of the language picked in the settings, or "" to follow the client's. */
    public static String getPickedLanguage() {
        try {
            String picked = ConfigPreferences.getString(LANGUAGE_PREF);
            return picked == null ? "" : picked;
        } catch (Throwable e) {
            return "";   // the preferences are not ready yet
        }
    }

    /** Picks {@code code} for Re: TeleVIP's texts; "" follows the client's language again. */
    public static void setPickedLanguage(String code) {
        if (code == null || code.isEmpty()) {
            ConfigPreferences.remove(LANGUAGE_PREF);
        } else {
            ConfigPreferences.putString(LANGUAGE_PREF, code);
        }
    }

    /** The client's language, as a language pack code (zh for every Chinese variant). */
    private static String clientLanguage() {
        if (localeController == null) return null;
        try {
            Locale locale = localeController.getCurrentLocale();
            return locale == null ? null : locale.getLanguage();
        } catch (Throwable e) {
            Logger.e(e);
            return null;
        }
    }

    /** The language Re: TeleVIP shows its texts in: the picked one, else the client's. */
    public static String getLanguage() {
        String picked = getPickedLanguage();
        if (!picked.isEmpty() && langMap.containsKey(picked)) return picked;
        return clientLanguage();
    }

    /** The pack for {@link #getLanguage}, or null when there is none (English fills in). */
    private static JSONObject currentPack() {
        String lang = getLanguage();
        return lang == null ? null : langMap.get(lang);
    }

    /**
     * Whether Re: TeleVIP's texts read right to left. Following the client, this is the client's
     * own direction, so the settings line up with the client's screens around them.
     */
    public static boolean isRTL() {
        String picked = getPickedLanguage();
        if (!picked.isEmpty() && langMap.containsKey(picked)) return isRTL(picked);
        try {
            return LocaleController.isRTL();
        } catch (Throwable e) {
            return isRTL(clientLanguage());
        }
    }

    private static boolean isRTL(String lang) {
        return "ar".equals(lang) || "fa".equals(lang);
    }

    /** Whether dates read day first ("12 March"), as in every shipped language but English and Chinese. */
    public static boolean isDayFirst() {
        String lang = getLanguage();
        if (lang == null || !langMap.containsKey(lang)) return isRTL();
        return !"en".equals(lang) && !"zh".equals(lang);
    }

    /** The name of {@code code} in its own language, as the picker lists it. */
    public static String nativeName(String code) {
        switch (code) {
            case "en": return "English";
            case "fa": return "فارسی";
            case "ru": return "Русский";
            case "ar": return "العربية";
            case "zh": return "简体中文";
            default: return code;
        }
    }

    /**
     * Runs {@code build} with the client's text direction set to Re: TeleVIP's, so the client's
     * cells it makes (switch rows, dialogs) line up with Re: TeleVIP's texts when the picked
     * language reads the other way than the client's. Those cells read the direction once, when
     * they are made; it is put back straight after.
     */
    public static void withTextDirection(Runnable build) {
        boolean swapped = false;
        boolean clientRtl = false;
        try {
            clientRtl = LocaleController.isRTL();
            boolean rtl = isRTL();
            if (rtl != clientRtl) {
                LocaleController.setRTL(rtl);
                swapped = true;
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
        try {
            build.run();
        } finally {
            if (swapped) {
                try {
                    LocaleController.setRTL(clientRtl);
                } catch (Throwable e) {
                    Logger.e(e);
                }
            }
        }
    }

    public static String get(String key) {
        String text = optional(key);
        return text != null ? text : key;
    }

    /** The text for {@code key}, or null when no language pack has it (unlike get, never the key). */
    public static String optional(String key) {
        if (key == null) return null;
        try {
            JSONObject langJson = currentPack();
            if (langJson != null && langJson.has(key)) return langJson.optString(key);
            JSONObject enJson = langMap.get("en");
            if (enJson != null && enJson.has(key)) return enJson.optString(key);
        } catch (Throwable e) {
            Logger.e(e);
        }
        return null;
    }

    public static String get(String key, Object... args) {
        String text = get(key);
        if (args == null || args.length == 0) return text;
        try {
            return String.format(text, args);
        } catch (Throwable e) {
            Logger.e(e);
            return text;
        }
    }

}
