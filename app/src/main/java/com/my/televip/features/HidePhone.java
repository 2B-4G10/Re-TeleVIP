package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.messenger.UserConfig;
import com.my.televip.virtuals.tgnet.TLRPC;

public class HidePhone {

    public static boolean isEnable = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                if (ClassLoad.getClass(ClassNames.USER_CONFIG) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.USER_CONFIG), AutomationResolver.resolve("UserConfig", "getClientUserId", AutomationResolver.ResolverType.Method), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.hidePhone.isEnable()) {
                                UserConfig userConfig = new UserConfig(param.thisObject);
                                if (userConfig.getCurrentUser().getUser() != null) {
                                    TLRPC.User user = userConfig.getCurrentUser();
                                    if (user.getPhone() != null) {
                                        user.setPhone(null);
                                    }
                                }
                            }
                        }
                    });
                }

                // With the number taken away, screens that print "+" + phone show "+null"
                // (the Settings header in Telegram 13 and its forks): show "Number hidden".
                if (ClassLoad.getClass(ClassNames.PHONE_FORMAT) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.PHONE_FORMAT), AutomationResolver.resolve("PhoneFormat", "format", AutomationResolver.ResolverType.Method), String.class, new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.hidePhone.isEnable() && "+null".equals(param.args[0])) {
                                param.setResult(Translator.get(Keys.PhoneHidden));
                            }
                        }
                    });
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

}
