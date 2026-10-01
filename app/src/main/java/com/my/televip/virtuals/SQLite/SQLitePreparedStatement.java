package com.my.televip.virtuals.SQLite;

import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.tgnet.NativeByteBuffer;

import com.my.televip.reflect.XReflect;

public class SQLitePreparedStatement {

    Object sQLitePreparedStatement;

    public SQLitePreparedStatement(Object sQLitePreparedStatement){
        this.sQLitePreparedStatement = sQLitePreparedStatement;
    }

    public void requery() {
        XReflect.callMethod(sQLitePreparedStatement, AutomationResolver.resolve("SQLitePreparedStatement","requery", AutomationResolver.ResolverType.Method));
    }

    public void step() {
        String step = AutomationResolver.resolve("SQLitePreparedStatement","step", AutomationResolver.ResolverType.Method);
        if (XReflect.findMethodExactIfExists(sQLitePreparedStatement.getClass(), step) != null) {
            XReflect.callMethod(sQLitePreparedStatement, step);
            return;
        }
        // step() only calls the native step(handle); R8 inlines it where nothing else calls it.
        long handle = XReflect.getLongField(sQLitePreparedStatement,
                AutomationResolver.resolve("SQLitePreparedStatement", "sqliteStatementHandle", AutomationResolver.ResolverType.Field));
        XReflect.callMethod(sQLitePreparedStatement, "step", handle);
    }

    public void dispose() {
        String dispose = AutomationResolver.resolve("SQLitePreparedStatement","dispose", AutomationResolver.ResolverType.Method);
        if (XReflect.findMethodExactIfExists(sQLitePreparedStatement.getClass(), dispose) != null) {
            XReflect.callMethod(sQLitePreparedStatement, dispose);
            return;
        }
        // dispose() only calls finalizeQuery(); R8 inlines it where nothing else calls it.
        XReflect.callMethod(sQLitePreparedStatement, AutomationResolver.resolve("SQLitePreparedStatement","finalizeQuery", AutomationResolver.ResolverType.Method));
    }

    public void bindByteBuffer(int index, NativeByteBuffer value) {
        XReflect.callMethod(sQLitePreparedStatement, AutomationResolver.resolve("SQLitePreparedStatement","bindByteBuffer", AutomationResolver.ResolverType.Method), index, value.nativeByteBuffer);
    }

    public void bindLong(int index, long value) {
        XReflect.callMethod(sQLitePreparedStatement, AutomationResolver.resolve("SQLitePreparedStatement","bindLong", AutomationResolver.ResolverType.Method), index, value);
    }

    public void bindInteger(int index, int value) {
        XReflect.callMethod(sQLitePreparedStatement, AutomationResolver.resolve("SQLitePreparedStatement","bindInteger", AutomationResolver.ResolverType.Method), index, value);
    }

}
