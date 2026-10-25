package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.utils.Utils;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.logging.Logger;

import com.my.televip.reflect.XReflect;

public class TelePremium {

    public static boolean isEnable = false;

    /** Set while a download starts, so isPremium answers for the account itself there. */
    private static final ThreadLocal<int[]> IN_DOWNLOAD_START = ThreadLocal.withInitial(() -> new int[1]);

    /**
     * A download asks isPremium how many connections to open: 8 for Premium, 2 otherwise. Told
     * yes for an account without Premium, it opened 8, which Telegram's servers answer with waits:
     * a 67 MB file took 23-27 s instead of 19-21 s on Nekogram. Downloads get the real answer.
     */
    private static void keepDownloadsAsTheAccount() {
        Class<?> operation = ClassLoad.getClass(ClassNames.FILE_LOAD_OPERATION);
        if (operation == null) return;
        String start = AutomationResolver.resolveOverload("FileLoadOperation", "startSJZ", "start");
        String connections = AutomationResolver.resolve("FileLoadOperation", "startConnections", AutomationResolver.ResolverType.Method);
        boolean hooked = false;
        for (java.lang.reflect.Method method : operation.getDeclaredMethods()) {
            Class<?>[] p = method.getParameterTypes();
            boolean isStart = method.getName().equals(start) && method.getReturnType() == boolean.class
                    && p.length == 3 && p[1] == long.class && p[2] == boolean.class;
            boolean isConnections = !"startConnections".equals(connections) && method.getName().equals(connections)
                    && method.getReturnType() == void.class && p.length == 0;
            if (!isStart && !isConnections) continue;
            HMethod.hookMethod(method, new AbstractMethodHook() {
                @Override
                protected void beforeMethod(MethodHookParam param) {
                    IN_DOWNLOAD_START.get()[0]++;
                }

                @Override
                protected void afterMethod(MethodHookParam param) {
                    IN_DOWNLOAD_START.get()[0]--;
                }
            });
            hooked = true;
        }
        if (hooked) return;
        Logger.w("Telegram Premium: FileLoadOperation#start not found, downloads use the Premium connection count");
    }

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                if (ClassLoad.getClass(ClassNames.USER_CONFIG) != null) {

                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.USER_CONFIG), AutomationResolver.resolve("UserConfig", "isPremium", AutomationResolver.ResolverType.Method), new AbstractMethodHook() {
                        @Override
                        public void beforeMethod(AbstractMethodHook.MethodHookParam param) {
                            if (ConfigManager.telegramPremium.isEnable() && IN_DOWNLOAD_START.get()[0] == 0) param.setResult(true);
                        }
                    });
                    keepDownloadsAsTheAccount();
                }
                if (ClientChecker.check(ClientChecker.ClientType.iMe) || ClientChecker.check(ClientChecker.ClientType.iMeWeb)) {
                    Class<?> ForkPremiumPreferencClass = XReflect.findClassIfExists("com.iMe.storage.data.locale.prefs.impl.ForkPremiumPreference", Utils.classLoader);
                    if (ForkPremiumPreferencClass != null) {
                        HMethod.hookMethod(ForkPremiumPreferencClass, "isPremium", new AbstractMethodHook() {
                            @Override
                            protected void beforeMethod(MethodHookParam param) {
                                if (ConfigManager.telegramPremium.isEnable())
                                    param.setResult(true);
                            }
                        });
                    }
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

}
