package com.my.televip.virtuals.messenger;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.tgnet.TLRPC;

import com.my.televip.reflect.XReflect;

public class UserConfig {

    Object userConfig;

    public UserConfig(Object obl){
        userConfig = obl;
    }

    public static int getSelectedAccount() {
        String selectedAccountField = AutomationResolver.resolve("UserConfig", "selectedAccount", AutomationResolver.ResolverType.Field);
        return XReflect.getStaticIntField(ClassLoad.getClass(ClassNames.USER_CONFIG), selectedAccountField);
    }

    /**
     * Whether the signed-in account really has Telegram Premium, read from the account itself:
     * UserConfig.isPremium answers true whenever "Telegram Premium" is on in Re: TeleVIP.
     */
    public static boolean hasRealPremium() {
        try {
            Class<?> type = ClassLoad.getClass(ClassNames.USER_CONFIG);
            Object[] instances = null;
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().isArray()
                        && field.getType().getComponentType() == type) {
                    field.setAccessible(true);
                    instances = (Object[]) field.get(null);
                    break;
                }
            }
            if (instances == null) return false;
            Object config = instances[getSelectedAccount()];
            if (config == null) return false;
            Object user = XReflect.getObjectField(config, AutomationResolver.resolve("UserConfig", "currentUser", AutomationResolver.ResolverType.Field));
            return user != null && Boolean.TRUE.equals(XReflect.getObjectField(user, AutomationResolver.resolve("TLRPC$User", "premium", AutomationResolver.ResolverType.Field)));
        } catch (Throwable t) {
            return false;
        }
    }

    public long getClientUserId(){
        return XReflect.getLongField(userConfig, AutomationResolver.resolve("UserConfig" , "clientUserId", AutomationResolver.ResolverType.Field));
    }

    public TLRPC.User getCurrentUser(){
        return new TLRPC.User(XReflect.callMethod(userConfig, AutomationResolver.resolve("UserConfig" , "getCurrentUser", AutomationResolver.ResolverType.Method)));
    }

}
