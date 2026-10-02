package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;

public class FixTLError {

    public static boolean isEnable = false;

    public static void init(){
        try {
            if (!isEnable) {
                isEnable = true;
                if (ClassLoad.getClass(ClassNames.LAUNCH_ACTIVITY) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.LAUNCH_ACTIVITY), AutomationResolver.resolve("LaunchActivity", "didReceivedNotification", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("didReceivedNotification", new Class<?>[]{int.class, int.class, Object[].class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.fixTLError.isEnable() && isTlParseError(param.args[2]))
                                param.setResult(null);
                        }
                    }));
                }
            }
        } catch (Throwable t){
            Logger.e(t);
    }
    }

    /**
     * The "TL Error" notification, recognised by what it carries rather than by its id, which
     * some builds give a renamed field: a TLParseException ("can't parse magic %x in %s. ...").
     */
    private static boolean isTlParseError(Object args) {
        if (!(args instanceof Object[]) || ((Object[]) args).length == 0) return false;
        Object error = ((Object[]) args)[0];
        return error instanceof RuntimeException
                && String.valueOf(((RuntimeException) error).getMessage()).startsWith("can't parse magic");
    }
}
