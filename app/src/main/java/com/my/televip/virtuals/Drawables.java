package com.my.televip.virtuals;

import android.content.Context;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.reflect.XReflect;

/**
 * Client drawable ids by name. R8 strips the R$drawable fields from release builds, so fall back
 * to the resource table, and to 0 (no icon) when the name is gone there too.
 */
public final class Drawables {

    private Drawables() {
    }

    public static int id(Context context, String... names) {
        Class<?> r = ClassLoad.getClass(ClassNames.DRAWABLE);
        for (String name : names) {
            if (r != null) {
                try {
                    int id = XReflect.getStaticIntField(r, name);
                    if (id != 0) return id;
                } catch (Throwable ignored) {
                }
            }
            if (context == null) continue;
            try {
                int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
                if (id != 0) return id;
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }
}
