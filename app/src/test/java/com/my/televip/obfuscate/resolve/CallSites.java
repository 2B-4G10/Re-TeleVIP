package com.my.televip.obfuscate.resolve;

import com.my.televip.obfuscate.dex.DexClass;
import com.my.televip.obfuscate.dex.DexIndex;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Every symbol the module's code asks the resolver for, read from the module's own sources.
 *
 * <p>The client checks are built from this list rather than from a hand-kept one, so a feature
 * added or changed later is covered without anyone remembering to list it. A call site is the
 * owner and name handed to {@code AutomationResolver.resolve} / {@code resolveOverload}, a method
 * list handed to {@code HMethod.hookMethod}, or a {@code ClassNames} class a feature loads.</p>
 */
final class CallSites {

    /** One symbol: a class ({@code name == null}), or a field or method of an owner. */
    static final class Site implements Comparable<Site> {
        final String kind, owner, name;
        final Set<String> files = new TreeSet<>();

        Site(String kind, String owner, String name) {
            this.kind = kind;
            this.owner = owner;
            this.name = name;
        }

        String key() {
            return name == null ? owner : owner + (kind.equals("Field") ? "." : "#") + name;
        }

        @Override
        public int compareTo(Site o) {
            return key().compareTo(o.key());
        }
    }

    private static final String RT = "AutomationResolver\\s*\\.\\s*ResolverType\\s*\\.\\s*(Method|Field)";
    private static final String ARG = "(\"[^\"]+\"|[A-Za-z_][A-Za-z0-9_.]*)";
    private static final Pattern RESOLVE_MEMBER = Pattern.compile(
            "AutomationResolver\\s*\\.\\s*resolve\\s*\\(\\s*" + ARG + "\\s*,\\s*" + ARG + "\\s*,\\s*" + RT + "\\s*\\)");
    private static final Pattern RESOLVE_OVERLOAD = Pattern.compile(
            "AutomationResolver\\s*\\.\\s*resolveOverload\\s*\\(\\s*" + ARG + "\\s*,\\s*\"([^\"]+)\"");
    private static final Pattern RESOLVE_CLASS = Pattern.compile(
            "AutomationResolver\\s*\\.\\s*resolve\\s*\\(\\s*" + ARG + "\\s*\\)");
    private static final Pattern HOOK_NAMES = Pattern.compile(
            "HMethod\\s*\\.\\s*hookMethod\\s*\\([^;]*?,\\s*\"([A-Za-z$]+)\"\\s*,\\s*new\\s+String\\s*\\[\\s*]\\s*\\{([^}]*)}");
    private static final Pattern CLASS_NAME_REF = Pattern.compile("ClassNames\\s*\\.\\s*([A-Z][A-Z0-9_]*)");
    private static final Pattern CONSTANT = Pattern.compile(
            "static\\s+final\\s+String\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*\"([^\"]*)\"\\s*;");
    private static final Pattern STRING = Pattern.compile("\"([^\"]+)\"");

    private CallSites() {
    }

    /**
     * Call sites a miss does not break, with the reason: the module has a working fallback of its
     * own, or the symbol only exists in older builds and the feature has another hook there.
     */
    static final Map<String, String> OPTIONAL = new LinkedHashMap<>();

    /**
     * Features reached by more than one route. Each group needs one route whose entries all
     * resolve; an entry {@code a|b} needs either.
     */
    static final Map<String, String[][]> ROUTES = new LinkedHashMap<>();

    static {
        OPTIONAL.put("TextCheckCell#isChecked", "inlined in some builds; the module remembers the last value it set");
        OPTIONAL.put("org.telegram.ui.Cells.ShadowSectionCell", "merged away in some builds; a plain spacer replaces it");
        OPTIONAL.put("SettingsActivity$SettingCell#set", "the row's icon is set from Factory.bindView instead");
        OPTIONAL.put("SecretMediaViewer#openMedia", "the old secret media viewer; ChatActivity's hooks cover current builds");
        OPTIONAL.put("SecretMediaViewer.onClose", "belt and braces: the read and delete requests are blocked already");
        OPTIONAL.put("org.telegram.messenger.R$drawable", "stripped by some builds; icons are looked up in the resource table");
        OPTIONAL.put("org.telegram.tgnet.TLRPC$TL_contacts_getSponsoredPeers",
                "R8 drops it from builds that never send it (NagramX removes search ads itself)");

        ROUTES.put("TeleVip's entry in the client's settings", new String[][]{
                {"org.telegram.ui.SettingsActivity", "org.telegram.ui.SettingsActivity$SettingCell$Factory",
                        "SettingsActivity$SettingCell$Factory#of|SettingsActivity$SettingCell$Factory#ofIIIICCC"},
                {"org.telegram.ui.Adapters.DrawerLayoutAdapter", "org.telegram.ui.Adapters.DrawerLayoutAdapter$Item",
                        "DrawerLayoutAdapter#resetItems", "DrawerLayoutAdapter#getId", "DrawerLayoutAdapter.items",
                        "DrawerLayoutContainer#closeDrawer", "LaunchActivity.drawerLayoutAdapter",
                        "LaunchActivity.drawerLayoutContainer"},
        });
        ROUTES.put("Jump to message", new String[][]{
                {"ChatActivity#scrollToMessageId|ChatActivity#scrollToMessageIdIIZIZIIABR"}});
        ROUTES.put("Photo viewer: parent activity", new String[][]{
                {"PhotoViewer#setParentActivity|PhotoViewer#setParentActivityAOO"}});
        ROUTES.put("Photo viewer: open a photo", new String[][]{
                {"PhotoViewer#openPhoto|PhotoViewer#openPhotoOOOOAAAIOOJJJZOI"}});
        ROUTES.put("Disable stories", new String[][]{
                {"StoriesController#hasStories|StoriesController#hasStories2"}});
    }

    /** The module's own settings list (its injected dex), not the client's. */
    static boolean isModuleOwn(Site site) {
        return site.owner.startsWith("com.televip.") || site.owner.equals("RecyclerListView");
    }

    /** What a client build is missing: call sites that break a feature, and fallbacks in use. */
    static final class Verdict {
        final Map<String, List<String>> broken = new LinkedHashMap<>();
        final List<String> degraded = new ArrayList<>();

        void breaks(String feature, String what) {
            List<String> list = broken.get(feature);
            if (list == null) broken.put(feature, list = new ArrayList<>());
            list.add(what);
        }
    }

    static Verdict verdict(List<Site> sites, List<Site> missing) {
        Map<String, Site> byKey = new HashMap<>();
        for (Site s : sites) byKey.put(s.key(), s);
        Set<String> miss = new TreeSet<>();
        for (Site s : missing) miss.add(s.key());

        Verdict v = new Verdict();
        Set<String> routed = new TreeSet<>();
        for (Map.Entry<String, String[][]> group : ROUTES.entrySet()) {
            boolean any = false;
            List<String> gaps = new ArrayList<>();
            for (String[] route : group.getValue()) {
                boolean all = true;
                for (String entry : route) {
                    boolean ok = false;
                    for (String key : entry.split("\\|")) {
                        if (!byKey.containsKey(key)) v.breaks("Stale ROUTES entry in CallSites", key);
                        routed.add(key);
                        ok |= byKey.containsKey(key) && !miss.contains(key);
                    }
                    if (!ok) {
                        all = false;
                        gaps.add(entry);
                    }
                }
                any |= all;
            }
            if (!any) v.breaks(group.getKey(), "no route resolves; missing " + String.join(", ", gaps));
        }
        for (String key : OPTIONAL.keySet()) {
            if (!byKey.containsKey(key)) v.breaks("Stale OPTIONAL entry in CallSites", key);
        }
        for (Site s : missing) {
            String key = s.key();
            if (isModuleOwn(s) || routed.contains(key)) continue;
            if (OPTIONAL.containsKey(key)) {
                v.degraded.add(key + " (" + OPTIONAL.get(key) + ")");
                continue;
            }
            for (String file : s.files) v.breaks(file.replace(".java", ""), key);
        }
        return v;
    }

    /** The module's main sources, from the repository root or the app module (Gradle's test directory). */
    static File sourceRoot() {
        for (String path : new String[]{"src/main/java", "app/src/main/java"}) {
            File dir = new File(path);
            if (new File(dir, "com/my/televip").isDirectory()) return dir;
        }
        throw new IllegalStateException("module sources not found from " + new File("").getAbsolutePath());
    }

    static List<Site> read() throws IOException {
        File root = sourceRoot();
        List<File> files = new ArrayList<>();
        collect(new File(root, "com/my/televip"), files);

        Map<String, String> classNames = constants(new File(root, "com/my/televip/Class/ClassNames.java"));
        Map<String, Site> sites = new TreeMap<>();
        for (File file : files) {
            String path = file.getPath().replace(File.separatorChar, '/');
            // The per-client name tables and the resolver itself are not call sites.
            if (path.contains("/Clients/") || path.contains("/obfuscate/") || path.endsWith("/ClassNames.java")) continue;
            String src = stripComments(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            Map<String, String> local = constants(src);
            String where = file.getName();

            Matcher m = RESOLVE_MEMBER.matcher(src);
            while (m.find()) {
                String owner = value(m.group(1), local, classNames), name = value(m.group(2), local, classNames);
                if (owner != null && name != null) add(sites, m.group(3), owner, name, where);
            }
            m = RESOLVE_OVERLOAD.matcher(src);
            while (m.find()) {
                String owner = value(m.group(1), local, classNames);
                if (owner != null) add(sites, "Method", owner, m.group(2), where);
            }
            m = RESOLVE_CLASS.matcher(src);
            while (m.find()) {
                String cls = value(m.group(1), local, classNames);
                if (cls != null && cls.contains(".")) add(sites, "Class", cls, null, where);
            }
            m = HOOK_NAMES.matcher(src);
            while (m.find()) {
                Matcher names = STRING.matcher(m.group(2));
                while (names.find()) add(sites, "Method", m.group(1), names.group(1), where);
            }
            m = CLASS_NAME_REF.matcher(src);
            while (m.find()) {
                String cls = classNames.get(m.group(1));
                if (cls != null) add(sites, "Class", cls, null, where);
            }
        }
        List<Site> list = new ArrayList<>(sites.values());
        Collections.sort(list);
        return list;
    }

    /**
     * The call sites that land on nothing in this APK, after resolving them the way the module
     * does at runtime: real names first, then the fingerprint mapping.
     */
    static List<Site> missing(DexIndex index, Mapping mapping, List<Site> sites) {
        Map<String, String> owners = TelegramFingerprints.owners();
        List<Site> missing = new ArrayList<>();
        for (Site site : sites) {
            if (site.name == null) {
                if (findClass(index, mapping, site.owner) == null) missing.add(site);
                continue;
            }
            String full = owners.get(site.owner);
            DexClass cls = full == null ? null : findClass(index, mapping, full);
            if (cls == null) {
                missing.add(site);
                continue;
            }
            String real = site.kind.equals("Field") ? mapping.resolveField(site.owner, site.name)
                    : mapping.resolveMethod(site.owner, TelegramFingerprints.methodKey(site.owner, site.name));
            boolean found;
            if (real != null) {
                found = has(index, cls, site.kind, real);
            } else {
                // Unmapped, the module asks for the name itself; an overload key falls back to
                // the plain name it stands for (see AutomationResolver.resolveOverload).
                String name = site.name.replace("storyEntitiesAllowed2", "storyEntitiesAllowed")
                        .replace("hasStories2", "hasStories");
                found = has(index, cls, site.kind, name)
                        || site.kind.equals("Method") && has(index, cls, site.kind, plainName(name));
            }
            if (!found) missing.add(site);
        }
        return missing;
    }

    /** Exactly as the module loads it: Class.forName on the resolved binary name. */
    private static DexClass findClass(DexIndex index, Mapping mapping, String name) {
        String resolved = mapping.resolveClass(name);
        return index.findClass(resolved != null ? resolved : name);
    }

    private static boolean has(DexIndex index, DexClass cls, String kind, String name) {
        for (DexClass k = cls; k != null; k = k.superclass == null ? null : index.byDescriptor(k.superclass)) {
            if (kind.equals("Field") ? k.fieldNamed(name) != null : !k.methodsNamed(name).isEmpty()) return true;
        }
        return false;
    }

    /** {@code getInputChannelO2} -> {@code getInputChannel}: the parameter suffix of a member key. */
    private static String plainName(String key) {
        return key.replaceAll("(?<=[a-z])[A-Z0-9]*$", "");
    }

    private static void add(Map<String, Site> sites, String kind, String owner, String name, String where) {
        Site site = new Site(kind, owner, name);
        Site known = sites.get(site.key());
        if (known == null) sites.put(site.key(), known = site);
        known.files.add(where);
    }

    private static String value(String arg, Map<String, String> local, Map<String, String> classNames) {
        if (arg.startsWith("\"")) return arg.substring(1, arg.length() - 1);
        if (arg.startsWith("ClassNames.")) return classNames.get(arg.substring("ClassNames.".length()));
        return local.get(arg);
    }

    private static Map<String, String> constants(File file) throws IOException {
        return constants(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    }

    private static Map<String, String> constants(String src) {
        Map<String, String> out = new HashMap<>();
        Matcher m = CONSTANT.matcher(src);
        while (m.find()) out.put(m.group(1), m.group(2));
        return out;
    }

    private static String stripComments(String src) {
        return src.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("(?m)^\\s*//.*$", " ");
    }

    private static void collect(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File f : children) {
            if (f.isDirectory()) collect(f, out);
            else if (f.getName().endsWith(".java")) out.add(f);
        }
    }

    /** Groups sites by the source file (feature) that uses them. */
    static Map<String, List<Site>> byFile(List<Site> sites) {
        Map<String, List<Site>> out = new LinkedHashMap<>();
        for (Site s : sites) {
            for (String f : s.files) {
                List<Site> list = out.get(f);
                if (list == null) out.put(f, list = new ArrayList<>());
                list.add(s);
            }
        }
        return out;
    }
}
