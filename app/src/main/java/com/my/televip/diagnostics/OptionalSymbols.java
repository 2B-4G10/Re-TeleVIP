package com.my.televip.diagnostics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbols a client build may lack without breaking anything, with the reason: the module has a
 * working fallback of its own, or the symbol only exists in some builds and the feature has
 * another hook there.
 *
 * <p>One list for both sides: at runtime a miss on one of these is logged as a fallback instead
 * of "missing" (see {@link HookHealth}), and the client checks (ClientApkTest, run weekly by Client
 * watch) do not count it as a broken feature. Keys are the names call sites use: a class's full
 * name, {@code Owner#method} or {@code Owner.field}.</p>
 */
public final class OptionalSymbols {

    private OptionalSymbols() {
    }

    public static final Map<String, String> REASONS;

    static {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("TextCheckCell#isChecked", "inlined in some builds; the module remembers the last value it set");
        r.put("org.telegram.ui.Cells.ShadowSectionCell", "merged away in some builds; a plain spacer replaces it");
        r.put("SettingsActivity$SettingCell#set", "the row's icon is set from Factory.bindView instead");
        r.put("SettingsActivity$SettingCell$Factory#bindView", "only paints the app icon on TeleVip's row");
        r.put("SettingsActivity$SettingCell.iconView", "only paints the app icon on TeleVip's row");
        r.put("org.telegram.ui.SettingsActivity$SettingCell", "only paints the app icon on TeleVip's row");
        r.put("StoriesController#hasStories", "inlined by R8 in some builds (Telegram 13.1); hasStories(long) still"
                + " hides each chat's stories");
        r.put("FileLoadOperation#startSJZ", "Nekogram 13 moves the part that reads isPremium into startConnections,"
                + " which is hooked there instead");
        r.put("FileLoadOperation#startConnections", "only in builds that split it out of start(stream, offset, preload),"
                + " which is hooked otherwise");
        r.put("MessagesController#markMessageContentAsRead", "only lets mentions clear with Hide seen on; reads"
                + " stay hidden without it");
        r.put("SecretMediaViewer#openMedia", "the old secret media viewer; ChatActivity's hooks cover current builds");
        r.put("SecretMediaViewer.onClose", "belt and braces: the read and delete requests are blocked already");
        r.put("MessageObject#isSecret", "inlined by Nekogram 12.9-12.10.1, which does not offer Secret media save;"
                + " the didPressImage hook clears the timer either way");
        r.put("FileLoader#getLocalFile", "only used inside the old secret media viewer's hook (see openMedia)");
        r.put("ImageReceiver#getImageLocation", "only used inside the old secret media viewer's hook (see openMedia)");
        r.put("org.telegram.messenger.R$drawable", "stripped by some builds; icons are looked up in the resource table");
        r.put("org.telegram.tgnet.TLRPC$TL_contacts_getSponsoredPeers",
                "R8 drops it from builds that never send it (NagramX removes search ads itself)");
        r.put("org.telegram.tgnet.TLRPC$TL_contacts_sponsoredPeersEmpty",
                "dropped along with contacts.getSponsoredPeers from builds that never send it (NagramX)");
        r.put("MessagesController#getSponsoredMessages",
                "Nagram and NagramX return no sponsored posts from it themselves, which R8 leaves nothing of;"
                        + " the ad requests and their answers are still blocked");
        REASONS = Collections.unmodifiableMap(r);
    }

    /** Why a build may lack {@code symbol}, or null when the module needs it. */
    public static String reason(String symbol) {
        return symbol == null ? null : REASONS.get(symbol);
    }
}
