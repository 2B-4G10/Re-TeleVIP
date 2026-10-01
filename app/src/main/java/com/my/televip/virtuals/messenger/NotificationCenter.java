package com.my.televip.virtuals.messenger;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;

import com.my.televip.reflect.XReflect;

public class NotificationCenter {


    private static int messagesDeleted = -1;


    public static int getMessagesDeleted() {
        if (messagesDeleted == -1) {
            messagesDeleted = XReflect.getStaticIntField(ClassLoad.getClass(ClassNames.NOTIFICATION_CENTER), AutomationResolver.resolve("NotificationCenter", "messagesDeleted", AutomationResolver.ResolverType.Field));
        }
        return messagesDeleted;
    }
}
