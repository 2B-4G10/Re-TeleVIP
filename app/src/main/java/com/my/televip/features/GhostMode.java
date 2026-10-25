package com.my.televip.features;

import android.widget.Toast;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.application.AndroidUtilities;
import com.my.televip.application.ApplicationLoaderHook;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;

import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.tgnet.TLConstructor;

import java.util.ArrayList;
import java.util.List;

public class GhostMode {

    public static boolean isEnable = false;

    /**
     * Set by the request hook on every request the client sends. The client sends requests
     * all the time, so if this is still false a while after start the hook never went in, and
     * nothing Ghost Mode hides is hidden.
     */
    private static volatile boolean requestsSeen = false;

    public static void init() {
        try {
            // Hooked whether or not an option is on yet: the hook checks the switches on every
            // request. Turning on a Hide seen option runs this before Hide seen itself counts as
            // on, so waiting for isGhostMode() left Ghost Mode off until a restart.
            if (!isEnable) {
                isEnable = true;
                HideSeen.initMentions();
                if (ClassLoad.getClass(ClassNames.CONNECTIONS_MANAGER) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CONNECTIONS_MANAGER), AutomationResolver.resolve("ConnectionsManager", "sendRequestInternal", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendRequestInternal", new Class<?>[]{ClassLoad.getClass(ClassNames.TL_OBJECT), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE_TIMESTAMP), ClassLoad.getClass(ClassNames.QUICK_ACK_DELEGATE), ClassLoad.getClass(ClassNames.WRITE_TO_SOCKET_DELEGATE), int.class, int.class, int.class, boolean.class, int.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            requestsSeen = true;
                            try {
                                if (HideSeen.isOwnRead(param.args[0])) {
                                    // Mark read after send: our own read, let through as is.
                                } else if (ConfigManager.isGhostMode()) {
                                    Object object = param.args[0];
                                    if (ClientChecker.check(ClientChecker.ClientType.Nagram)) {
                                        HideSeen.saveReadHistory(object);
                                    }
                                    if (ConfigManager.hideOnline.isEnable()) {
                                        if (isOnlineRequest(object))
                                            XReflect.setBooleanField(object, AutomationResolver.resolve("TL_account$updateStatus", "offline", AutomationResolver.ResolverType.Field), true);
                                    }

                                    if (ConfigManager.hideSeen.isEnable() && HideSeen.isReadMessageRequest(object)) {
                                        HideSeen.sendFakeReadResponse(param.args[1]);
                                        param.setResult(null);
                                        return;
                                    }

                                    if (ConfigManager.hideTyping.isEnable() && HideTyping.isTypingRequest(object)) {
                                        param.setResult(null);
                                        return;
                                    }

                                    if (ConfigManager.hideStoryView.isEnable() && HideStoryRead.isReadStoriesRequest(object)) {
                                        param.setResult(null);
                                        return;
                                    }

                                    HideSeen.handleReadAfterSend(object);
                                }
                            } catch (Throwable e) {
                                Logger.e(e);
                            }
                        }
                    }));
                }
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * The Ghost Mode options that are on but cannot work on this build of the client: all of them
     * when the request hook is not in, else those whose request could not be found. Empty when
     * every option that is on works (or none is on).
     */
    public static List<String> notWorking() {
        List<String> broken = new ArrayList<>();
        if (ConfigManager.hideSeen == null || !ConfigManager.isGhostMode()) return broken;
        boolean hooked = isEnable && requestsSeen;
        boolean obfuscated = ClientChecker.isTgnetObfuscated();
        if (ConfigManager.hideSeen.isEnable() && (!hooked || (obfuscated
                && (ClassLoad.getClass(ClassNames.TL_MESSAGES_READ_HISTORY) == null
                || ClassLoad.getClass(ClassNames.TL_CHANNELS_READ_HISTORY) == null)))) {
            broken.add(Keys.HideSeen);
        }
        if (ConfigManager.markReadAfterSend.isEnable() && !hooked) {
            broken.add(Keys.MarkReadAfterSend);
        }
        if (ConfigManager.hideTyping.isEnable() && (!hooked
                || (obfuscated && ClassLoad.getClass(ClassNames.TL_MESSAGES_SET_TYPING) == null))) {
            broken.add(Keys.HideTyping);
        }
        if (ConfigManager.hideStoryView.isEnable() && (!hooked
                || (obfuscated && ClassLoad.getClass(ClassNames.TL_STORIES_READ_STORIES) == null))) {
            broken.add(Keys.HideStoryView);
        }
        if (ConfigManager.hideOnline.isEnable() && (!hooked
                || (obfuscated && ClassLoad.getClass(ClassNames.TL_ACCOUNT_UPDATE_STATUS) == null))) {
            broken.add(Keys.HideOnline);
        }
        return broken;
    }

    /** The warning for {@link #notWorking()}, or null when Ghost Mode works. */
    public static String warning() {
        List<String> broken = notWorking();
        if (broken.isEmpty()) return null;
        StringBuilder names = new StringBuilder();
        for (String key : broken) {
            if (names.length() > 0) names.append(", ");
            names.append(Translator.get(key));
        }
        return Translator.get(Keys.GhostModeNotWorking, names.toString());
    }

    /**
     * Tells the user once per start when an option they turned on cannot work on this build,
     * instead of letting reads, typing or online status go out while the switch says otherwise.
     * Checked a while after start, once the client has sent its first requests.
     */
    public static void warnIfNotWorking() {
        AndroidUtilities.runOnUIThread(() -> {
            try {
                String warning = warning();
                if (warning == null) return;
                Logger.w("Ghost Mode is on but not working: " + notWorking());
                Toast.makeText(ApplicationLoaderHook.getApplicationContext(), warning, Toast.LENGTH_LONG).show();
            } catch (Throwable t) {
                Logger.e(t);
            }
        }, 20_000);
    }

    public static boolean isOnlineRequest(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
            return object.getClass().getName().contains("TL_account$updateStatus");
        } else {
            return TLConstructor.is(object, ClassNames.TL_ACCOUNT_UPDATE_STATUS, TLConstructor.ACCOUNT_UPDATE_STATUS);
        }
    }

}
