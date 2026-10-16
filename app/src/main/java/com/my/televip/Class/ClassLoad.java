package com.my.televip.Class;

import com.my.televip.diagnostics.HookHealth;
import com.my.televip.diagnostics.OptionalSymbols;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import com.my.televip.reflect.XReflect;

public class ClassLoad {

    private static final Map<String, Class<?>> cache = new HashMap<>();

    public static Class<?> getClass(String name) {
        String resolved = AutomationResolver.resolve(name);

        if (cache.containsKey(resolved)) {
            return cache.get(resolved);
        }

        try {
            Class<?> cls = XReflect.findClassIfExists(
                    resolved,
                    Utils.classLoader
            );
            // A miss is remembered too, so it is counted and logged once, not at every lookup.
            cache.put(resolved, cls);
            if (cls == null) notFound(name, resolved);
            return cls;

        } catch (Throwable e) {
            Logger.e(e);
            return null;
        }
    }

    public static Class<?> getClass(String name, ClassLoader classLoader) {
        String resolved = AutomationResolver.resolve(name);

        if (cache.containsKey(resolved)) {
            return cache.get(resolved);
        }

        try {
            Class<?> cls = XReflect.findClassIfExists(
                    resolved,
                    classLoader
            );
            // A miss is remembered too, so it is counted and logged once, not at every lookup.
            cache.put(resolved, cls);
            if (cls == null) notFound(name, resolved);
            return cls;

        } catch (Throwable e) {
            Logger.e(e);
            return null;
        }
    }

    /** A class the build lacks: a fallback when it is one builds may drop, a fault otherwise. */
    private static void notFound(String name, String resolved) {
        if (HookHealth.isOptional(name)) {
            HookHealth.missingClass(name);
            Logger.l("Not in this build: " + name + " (" + OptionalSymbols.reason(name) + ")");
            return;
        }
        HookHealth.missingClass(resolved);
        Logger.w("Not found " + name + ", " + resolved + " " + Utils.issue);
    }

}
