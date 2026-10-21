package com.my.televip.features;

import com.my.televip.Configs.ConfigPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The self-destructing media the user opened with "Prevent media" on: the sender was told it was
 * opened, so the server lets it expire, but this phone keeps it. Saved across restarts, so a
 * later history load from the server (which by then has the expired copy) does not replace it.
 */
final class KeptSecretMedia {

    private static final String PREF = "KeptSecretMedia";
    /** Oldest entries go first beyond this; secret media is rare, and the list stays small. */
    private static final int LIMIT = 500;

    private static LinkedHashSet<String> kept;
    /** The message ids alone, for the cheap first check. */
    private static final HashSet<Integer> mids = new HashSet<>();

    private KeptSecretMedia() {
    }

    private static String key(long dialogId, int mid) {
        return dialogId + ":" + mid;
    }

    private static LinkedHashSet<String> load() {
        if (kept == null) {
            kept = new LinkedHashSet<>();
            String saved = ConfigPreferences.getString(PREF);
            if (saved != null && !saved.isEmpty()) {
                for (String entry : saved.split(",")) {
                    if (!entry.isEmpty() && kept.add(entry)) addMid(entry);
                }
            }
        }
        return kept;
    }

    private static void save() {
        ConfigPreferences.putString(PREF, String.join(",", kept));
        mids.clear();
        for (String entry : kept) addMid(entry);
    }

    private static void addMid(String entry) {
        try {
            mids.add(Integer.parseInt(entry.substring(entry.indexOf(':') + 1)));
        } catch (NumberFormatException ignored) {
        }
    }

    static synchronized void add(long dialogId, int mid) {
        LinkedHashSet<String> set = load();
        if (!set.add(key(dialogId, mid))) return;
        if (set.size() > LIMIT) {
            List<String> all = new ArrayList<>(set);
            set.clear();
            set.addAll(all.subList(all.size() - LIMIT, all.size()));
        }
        save();
    }

    static synchronized void remove(long dialogId, int mid) {
        if (load().remove(key(dialogId, mid))) save();
    }

    static synchronized boolean contains(long dialogId, int mid) {
        return load().contains(key(dialogId, mid));
    }

    /** Cheap first check for history loads: is any kept media this message id? */
    static synchronized boolean containsMid(int mid) {
        load();
        return mids.contains(mid);
    }

    static synchronized boolean isEmpty() {
        return load().isEmpty();
    }
}
