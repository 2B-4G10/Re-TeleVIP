package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;

import java.lang.reflect.Method;

public class DisableNumberRounding {

    public static boolean isEnable = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                Class<?> localeController = ClassLoad.getClass(ClassNames.LOCALE_CONTROLLER);
                if (localeController != null) {
                    // The build's own parameter order: R8 swaps them in some (Nekogram 12.10.5+).
                    Method format = XReflect.findMethodExactIfExists(localeController,
                            AutomationResolver.resolve("LocaleController", "formatShortNumber", AutomationResolver.ResolverType.Method),
                            AutomationResolver.resolveObject("formatShortNumber", new Class[]{int.class, int[].class}));
                    HMethod.hookMethod(format, new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.disableNumberRounding.isEnable()) {
                                int number = 0;
                                int[] rounded = null;
                                for (Object arg : param.args) {
                                    if (arg instanceof Integer) number = (Integer) arg;
                                    else if (arg instanceof int[]) rounded = (int[]) arg;
                                }
                                if (rounded != null) {
                                    rounded[0] = number;
                                }
                                param.setResult(String.valueOf(number));
                            }
                        }
                    });
                }
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }
}
