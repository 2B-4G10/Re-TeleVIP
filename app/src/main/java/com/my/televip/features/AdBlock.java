package com.my.televip.features;

import android.content.SharedPreferences;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.application.AndroidUtilities;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.messenger.MessagesController;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Blocks Telegram's ads where they come from: the requests that fetch them.
 *
 * <ul>
 *   <li>{@code messages.getSponsoredMessages}: sponsored posts in channels and bot chats, and the
 *       ads the video player shows.</li>
 *   <li>{@code contacts.getSponsoredPeers}: the "Ad" results in chat search.</li>
 *   <li>{@code help.getPromoData}: the proxy sponsor / PSA channel pinned to the top of the chat
 *       list.</li>
 * </ul>
 *
 * <p>Dropped requests never reach the server, so no ad is downloaded and the client has nothing
 * to show. The promo channel needs more: one fetched before the module was enabled is saved and
 * comes back on every start, so the periodic promo check is replaced by removing it.</p>
 */
public class AdBlock {

    private static final String[] AD_REQUESTS = {
            ClassNames.TL_MESSAGES_GET_SPONSORED_MESSAGES,
            ClassNames.TL_CONTACTS_GET_SPONSORED_PEERS,
            ClassNames.TL_HELP_GET_PROMO_DATA,
    };

    /** Where checkPromoInfoInternal saves the promo channel, which the client restores on start. */
    private static final String[] PROMO_KEYS = {
            "proxy_dialog", "proxyDialogAddress", "nextPromoInfoCheckTime",
            "promo_dialog_type", "promo_psa_message", "promo_psa_type",
    };

    public static boolean isEnable = false;

    private static volatile Set<Class<?>> adRequests = Collections.emptySet();
    private static volatile Method removePromoDialog;
    private static volatile boolean promoRemovalFailed;

    public static void init() {
        if (isEnable) return;
        isEnable = true;
        try {
            blockAdRequests();
        } catch (Throwable t) {
            Logger.e(t);
        }
        try {
            removePromoChannel();
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    private static boolean enabled() {
        return ConfigManager.blockAds != null && ConfigManager.blockAds.isEnable();
    }

    private static void blockAdRequests() {
        Class<?> connectionsManager = ClassLoad.getClass(ClassNames.CONNECTIONS_MANAGER);
        if (connectionsManager == null) return;

        Set<Class<?>> requests = new HashSet<>();
        for (String name : AD_REQUESTS) {
            Class<?> request = ClassLoad.getClass(name);
            if (request != null) requests.add(request);
        }
        if (requests.isEmpty()) return;
        adRequests = requests;

        // Every request goes through sendRequestInternal on the stage queue, whichever sendRequest
        // overload the caller used.
        HMethod.hookMethod(connectionsManager,
                AutomationResolver.resolve("ConnectionsManager", "sendRequestInternal", AutomationResolver.ResolverType.Method),
                AutomationResolver.merge(AutomationResolver.resolveObject("sendRequestInternal", new Class[]{ClassLoad.getClass(ClassNames.TL_OBJECT), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE_TIMESTAMP), ClassLoad.getClass(ClassNames.QUICK_ACK_DELEGATE), ClassLoad.getClass(ClassNames.WRITE_TO_SOCKET_DELEGATE), int.class, int.class, int.class, boolean.class, int.class}), new AbstractMethodHook() {
                    @Override
                    protected void beforeMethod(MethodHookParam param) {
                        Object request = param.args[0];
                        if (request != null && adRequests.contains(request.getClass()) && enabled()) {
                            // The caller's callback never runs: to it the ads are still loading.
                            param.setResult(null);
                        }
                    }
                }));
    }

    private static void removePromoChannel() {
        Class<?> messagesController = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
        if (messagesController == null) return;

        removePromoDialog = XReflect.findMethodExactIfExists(messagesController,
                AutomationResolver.resolve("MessagesController", "removePromoDialog", AutomationResolver.ResolverType.Method));

        // Runs about once a second from updateTimerProc, and when the account or proxy changes. It
        // used to be hooked after the fact, by which time it had asked the server for the promo
        // channel, and the answer put the channel back right after it was removed.
        HMethod.hookMethod(messagesController,
                AutomationResolver.resolve("MessagesController", "checkPromoInfoInternal", AutomationResolver.ResolverType.Method),
                AutomationResolver.merge(AutomationResolver.resolveObject("checkPromoInfoInternal", new Class[]{boolean.class}), new AbstractMethodHook() {
                    @Override
                    protected void beforeMethod(MethodHookParam param) {
                        if (!enabled()) return;
                        param.setResult(null);
                        forgetSavedPromo();
                        final Object controller = param.thisObject;
                        if (removePromoDialog != null && !promoRemovalFailed) {
                            AndroidUtilities.runOnUIThread(() -> removePromoDialog(controller));
                        }
                    }
                }));
    }

    /** Takes a saved promo channel out of the chat list; a no-op once there is none. */
    private static void removePromoDialog(Object controller) {
        try {
            removePromoDialog.invoke(controller);
        } catch (Throwable t) {
            // Called every second: report it once instead of flooding the log.
            promoRemovalFailed = true;
            Logger.e(t);
        }
    }

    private static volatile SharedPreferences mainSettings;
    private static volatile boolean mainSettingsFailed;

    /** So the client does not load the promo channel again on its next start. */
    private static void forgetSavedPromo() {
        if (mainSettingsFailed) return;
        try {
            if (mainSettings == null) mainSettings = MessagesController.getGlobalMainSettings();
            if (mainSettings == null || !mainSettings.contains(PROMO_KEYS[0])) return;
            SharedPreferences.Editor editor = mainSettings.edit();
            for (String key : PROMO_KEYS) editor.remove(key);
            editor.apply();
        } catch (Throwable t) {
            mainSettingsFailed = true;
            Logger.e(t);
        }
    }
}
