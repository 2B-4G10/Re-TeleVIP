package com.my.televip.updates;

import java.util.ArrayList;
import java.util.List;

/** The pieces of a GitHub release the update check needs, and the plain-Java work on them. */
public final class Release {

    public final String tag;
    public final String version;
    public final String url;
    public final String notes;

    public Release(String tag, String url, String body) {
        this.tag = tag;
        this.version = stripV(tag);
        this.url = url;
        this.notes = notes(body);
    }

    /** "v1.3" and "1.3" both read as "1.3". */
    static String stripV(String tag) {
        if (tag == null) return "";
        String t = tag.trim();
        return t.startsWith("v") || t.startsWith("V") ? t.substring(1) : t;
    }

    /**
     * Whether {@code latest} is a newer version than {@code installed}, comparing their numbers
     * part by part (1.10 is newer than 1.9, 1.3 and 1.3.0 are the same). False when either has no
     * number to compare, so a tag in an unexpected shape never prompts anyone.
     */
    public static boolean isNewer(String latest, String installed) {
        List<Integer> a = numbers(stripV(latest));
        List<Integer> b = numbers(stripV(installed));
        if (a.isEmpty() || b.isEmpty()) return false;
        for (int i = 0; i < Math.max(a.size(), b.size()); i++) {
            int x = i < a.size() ? a.get(i) : 0;
            int y = i < b.size() ? b.get(i) : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    /** The leading dotted numbers: "1.3.2-beta" gives 1, 3, 2. */
    private static List<Integer> numbers(String version) {
        List<Integer> parts = new ArrayList<>();
        for (String part : version.split("\\.")) {
            int end = 0;
            while (end < part.length() && Character.isDigit(part.charAt(end))) end++;
            if (end == 0 || end > 9) break;
            parts.add(Integer.parseInt(part.substring(0, end)));
            if (end < part.length()) break;
        }
        return parts;
    }

    /**
     * The release's notes as plain text for a dialog: what's new only (the install steps and
     * anything after the first "###" heading are left out), bold marks removed, list items as
     * bullets, and each item's wrapped lines joined back into one paragraph.
     */
    static String notes(String body) {
        if (body == null) return "";
        StringBuilder out = new StringBuilder();
        StringBuilder item = null;
        for (String raw : body.replace("\r", "").split("\n")) {
            String line = raw.trim();
            if (line.startsWith("#")) {
                if (out.length() > 0 || item != null) break;
                continue;
            }
            if (line.isEmpty()) {
                item = flush(out, item);
                continue;
            }
            if (line.startsWith("- ") || line.startsWith("* ")) {
                item = flush(out, item);
                item = new StringBuilder("• ").append(line.substring(2).trim());
            } else if (item == null) {
                item = new StringBuilder(line);
            } else {
                item.append(' ').append(line);
            }
        }
        flush(out, item);
        return out.toString().replace("**", "").replace("`", "").trim();
    }

    private static StringBuilder flush(StringBuilder out, StringBuilder item) {
        if (item != null && item.length() > 0) {
            if (out.length() > 0) out.append("\n\n");
            out.append(item);
        }
        return null;
    }
}
