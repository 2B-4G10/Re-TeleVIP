package com.my.televip.obfuscate.resolve;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.my.televip.obfuscate.dex.DexIndex;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks that every feature's hook points resolve in a client APK - any Telegram client, any build.
 * Run by the weekly Client watch workflow against each client's latest release:
 *
 * <pre>TELEVIP_CLIENT_APK=client.apk [TELEVIP_CLIENT_REPORT=report.md] gradlew :app:testDebugUnitTest --tests '*ClientApkTest'</pre>
 */
public class ClientApkTest {

    private static final String FIELD = ".", METHOD = "#";

    /** Hook point (owner + separator + key) -> the feature that stops working without it. */
    private static final Map<String, String> HOOK_POINTS = new LinkedHashMap<>();

    static {
        hook("Ghost Mode settings page", "LaunchActivity.frameLayout");
        hook("Hide pinned messages", "ChatActivity#updatePinnedMessageViewZI", "ChatActivity#createPinnedMessageView",
                "ChatActivity.pinnedMessageView");
        hook("Remove content-saving restrictions", "ChatActivity#hasSelectedNoforwardsMessage");
        hook("Save edits history", "ChatActivity#processSelectedOption", "ChatActivity#fillMessageMenu",
                "ChatActivity.selectedObject");
        hook("Show deleted messages", "ChatMessageCell#measureTime", "ChatMessageCell.currentTimeString",
                "ChatMessageCell.timeWidth", "ChatMessageCell.timeTextWidth", "Theme.chat_timePaint");
        hook("Prevent media deletion", "ChatActivity#sendSecretMediaDelete", "ChatActivity#sendSecretMessageRead");
        hook("Secret media save", "ChatActivity$ChatMessageCellDelegate#didPressImage");
        hook("Save protected stories", "PeerStoriesView$StoryItemHolder#allowScreenshots");
        hook("Always save media", "PhotoViewer#setIsAboutToSwitchToIndexIZZZ", "PhotoViewer.galleryButton");
        hook("Chat and profile menu entries", "ActionBarMenuItem#lazilyAddSubItem", "ActionBarMenuItem#addSubItem",
                "ChatActivity.headerItem", "ProfileActivity.otherItem", "ProfileActivity#createActionBarMenu");
        hook("Profile user ID / online status", "ProfileActivity#updateProfileData", "ProfileActivity.userId",
                "ProfileActivity.onlineTextView");
    }

    private static void hook(String feature, String... points) {
        for (String p : points) HOOK_POINTS.put(p, feature);
    }

    @Test
    public void everyFeatureHookPointResolves() throws Exception {
        String path = System.getenv("TELEVIP_CLIENT_APK");
        assumeTrue("set TELEVIP_CLIENT_APK to run", path != null && new File(path).isFile());

        Resolver resolver = new Resolver(DexIndex.fromApk(new File(path)), TelegramFingerprints.owners());
        Resolver.Report report = new Resolver.Report();
        Mapping mapping = resolver.resolve(TelegramFingerprints.all(), report);

        Map<String, List<String>> broken = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : HOOK_POINTS.entrySet()) {
            if (resolve(mapping, e.getKey()) == null) {
                List<String> points = broken.get(e.getValue());
                if (points == null) broken.put(e.getValue(), points = new ArrayList<>());
                points.add(e.getKey());
            }
        }

        StringBuilder md = new StringBuilder();
        md.append("Symbols: ").append(report.count(Resolver.Outcome.KEPT)).append(" by real name, ")
                .append(report.count(Resolver.Outcome.FINGERPRINTED)).append(" fingerprinted, ")
                .append(report.count(Resolver.Outcome.AMBIGUOUS)).append(" ambiguous, ")
                .append(report.count(Resolver.Outcome.UNRESOLVED)).append(" unresolved.\n\n");
        if (broken.isEmpty()) {
            md.append("All ").append(HOOK_POINTS.size()).append(" feature hook points resolve.\n");
        } else {
            md.append("| Feature | Unresolved hook points |\n|---|---|\n");
            for (Map.Entry<String, List<String>> e : broken.entrySet()) {
                md.append("| ").append(e.getKey()).append(" | `")
                        .append(String.join("`, `", e.getValue())).append("` |\n");
            }
        }
        writeReport(md.toString());

        if (!broken.isEmpty()) fail("Features broken on this build:\n" + md);
    }

    /**
     * Wherever this build keeps a real name, the fingerprint alone must find the same symbol - or
     * nothing. A fingerprint that picks another symbol here would hook the wrong code on a fork
     * that renames it.
     */
    @Test
    public void fingerprintsNeverContradictRealNames() throws Exception {
        String path = System.getenv("TELEVIP_CLIENT_APK");
        assumeTrue("set TELEVIP_CLIENT_APK to run", path != null && new File(path).isFile());

        DexIndex index = DexIndex.fromApk(new File(path));
        Mapping named = new Resolver(index, TelegramFingerprints.owners())
                .resolve(TelegramFingerprints.all(), new Resolver.Report());
        Mapping fingerprinted = new Resolver(index, TelegramFingerprints.owners()).fingerprintsOnly()
                .resolve(TelegramFingerprints.all(), new Resolver.Report());

        List<String> wrong = new ArrayList<>();
        compare(named.classes(), fingerprinted.classes(), wrong);
        compare(named.fields(), fingerprinted.fields(), wrong);
        compare(named.methods(), fingerprinted.methods(), wrong);
        if (!wrong.isEmpty()) fail("Fingerprints that find the wrong symbol:\n" + String.join("\n", wrong));
    }

    private static void compare(Map<String, String> truth, Map<String, String> found, List<String> wrong) {
        for (Map.Entry<String, String> e : truth.entrySet()) {
            String got = found.get(e.getKey());
            if (got != null && !got.equals(e.getValue())) {
                wrong.add(e.getKey() + ": " + got + " instead of " + e.getValue());
            }
        }
    }

    private static String resolve(Mapping mapping, String point) {
        int m = point.indexOf(METHOD);
        if (m >= 0) return mapping.resolveMethod(point.substring(0, m), point.substring(m + 1));
        int f = point.lastIndexOf(FIELD);
        return mapping.resolveField(point.substring(0, f), point.substring(f + 1));
    }

    private static void writeReport(String text) throws Exception {
        String out = System.getenv("TELEVIP_CLIENT_REPORT");
        if (out == null) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            w.write(text);
        }
    }
}
