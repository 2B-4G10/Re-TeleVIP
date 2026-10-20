package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.ui.SecretMediaViewer;
import com.my.televip.xposed.XBridge;

import com.my.televip.reflect.XReflect;
import com.my.televip.utils.Utils;

public class PreventMedia {

    public static boolean isEnable = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                if (ClassLoad.getClass(ClassNames.CHAT_ACTIVITY) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "sendSecretMessageRead", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendSecretMessageRead", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT), boolean.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.preventMedia.isEnable()) param.setResult(null);
                        }
                    }));
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "sendSecretMediaDelete", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendSecretMediaDelete", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT)}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.preventMedia.isEnable()) param.setResult(null);
                        }
                    }));
                }

                // sendSecretMessageRead and sendSecretMediaDelete are small private methods, and
                // ART compiles them into the chat's tap handlers (Telegram 13.1 on Android 15):
                // the hooks above then never run and an opened photo still expires. Running the
                // handlers interpreted makes them call the hooked methods.
                // Looked up quietly: builds that rename it (Nekogram) keep it under another name,
                // and a miss here is not worth the health report's "incompatible version" warning.
                Class<?> cellDelegate = XReflect.findClassIfExists(
                        AutomationResolver.resolve(ClassNames.CHAT_MESSAGE_CELL_DELEGATE), Utils.classLoader);
                if (cellDelegate != null) {
                    for (java.lang.reflect.Method method : cellDelegate.getDeclaredMethods()) {
                        XBridge.deoptimize(method);
                    }
                }

                if (ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER) != null) {
                    SecretMediaViewer.openMedia();
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER), AutomationResolver.resolve("SecretMediaViewer", "closePhoto", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("closePhoto", new Class<?>[]{boolean.class, boolean.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.preventMedia.isEnable()) {
                                // Belt and braces: the read/delete requests are already blocked
                                // above, so a build where onClose is renamed loses nothing.
                                try {
                                    XReflect.setObjectField(param.thisObject, AutomationResolver.resolve("SecretMediaViewer", "onClose", AutomationResolver.ResolverType.Field), null);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    }));
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

}
