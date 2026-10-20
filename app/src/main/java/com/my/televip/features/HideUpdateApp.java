package com.my.televip.features;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.application.ApplicationLoaderHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.my.televip.base.MethodReplacement;

public class HideUpdateApp {

    public static boolean isEnable = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                SharedPreferences preferences = ApplicationLoaderHook.getApplicationContext().getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                preferences.edit().remove("appUpdate").apply();
                preferences.edit().remove("appUpdateCheckTime").apply();
                preferences.edit().remove("appUpdateBuild").apply();

                if (ClassLoad.getClass(ClassNames.SHARED_CONFIG) != null) {

                    HMethod.hookMethod(
                            ClassLoad.getClass(ClassNames.SHARED_CONFIG),
                            AutomationResolver.resolve("SharedConfig", "setNewAppVersionAvailable", AutomationResolver.ResolverType.Method),
                            AutomationResolver.merge(AutomationResolver.resolveObject("setNewAppVersionAvailable", new Class<?>[]{ClassLoad.getClass(ClassNames.TL_HELP_APP_UPDATE)}), new MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam param) {
                                    // void in builds where it always answered true.
                                    return ((Method) param.method).getReturnType() == void.class ? null : false;
                                }
                            }));

                    HMethod.hookMethod(
                            ClassLoad.getClass(ClassNames.SHARED_CONFIG),
                            AutomationResolver.resolve("SharedConfig", "isAppUpdateAvailable", AutomationResolver.ResolverType.Method), new MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(MethodHookParam param) {
                                    return false;
                                }
                            });
                }

                // Nekogram shows the update pop-up (and the full-screen "update now" page) straight
                // from its own update check, without asking SharedConfig, so both are switched off
                // too. They are found by their parameters: the pop-up is on the client's own
                // ApplicationLoader subclass, which a hook on ApplicationLoader's would miss.
                Class<?> appUpdate = ClassLoad.getClass(ClassNames.TL_HELP_APP_UPDATE);
                if (appUpdate != null) {
                    Context application = ApplicationLoaderHook.getApplicationContext();
                    for (Class<?> c = application == null ? null : application.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                        hookAll(c, Context.class, appUpdate, int.class);
                    }
                    hookAll(ClassLoad.getClass(ClassNames.LAUNCH_ACTIVITY), int.class, appUpdate, boolean.class);
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

    /** Turns every method of {@code owner} declared with exactly {@code params} into a no-op. */
    private static void hookAll(Class<?> owner, Class<?>... params) {
        if (owner == null) return;
        for (Method method : owner.getDeclaredMethods()) {
            if (!Arrays.equals(method.getParameterTypes(), params)) continue;
            HMethod.hookMethod(method, new MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return method.getReturnType() == boolean.class ? false : null;
                }
            });
        }
    }
}
