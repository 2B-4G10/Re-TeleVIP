package com.my.televip.virtuals.messenger;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.SQLite.SQLiteDatabase;

import com.my.televip.reflect.XReflect;

public class MessagesStorage {

    Object messagesStorage;

    public MessagesStorage(Object obj) {
        messagesStorage = obj;
    }

    public SQLiteDatabase getDatabase() {
        return new SQLiteDatabase(getterOrField(
                AutomationResolver.resolve("MessagesStorage", "getDatabase", AutomationResolver.ResolverType.Method),
                AutomationResolver.resolve("MessagesStorage", "database", AutomationResolver.ResolverType.Field)));
    }

    public DispatchQueue getStorageQueue() {
        return new DispatchQueue(getterOrField(
                AutomationResolver.resolve("MessagesStorage", "getStorageQueue", AutomationResolver.ResolverType.Method),
                AutomationResolver.resolve("MessagesStorage", "storageQueue", AutomationResolver.ResolverType.Field)));
    }

    /** A getter, or the field it returns where R8 inlined the getter (Nekogram 12.10.5+). */
    private Object getterOrField(String getter, String field) {
        if (XReflect.findMethodExactIfExists(messagesStorage.getClass(), getter) != null) {
            return XReflect.callMethod(messagesStorage, getter);
        }
        return XReflect.getObjectField(messagesStorage, field);
    }

    public static MessagesStorage getInstance(int num) {
        return new MessagesStorage(XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_STORAGE), AutomationResolver.resolve("MessagesStorage", "getInstance", AutomationResolver.ResolverType.Method), num));
    }

}
