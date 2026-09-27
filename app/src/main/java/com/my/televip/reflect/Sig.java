package com.my.televip.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Finds a method by its shape instead of its name, for when R8 has renamed it and no mapping
 * names it: the one instance method of the object's class hierarchy with exactly these
 * parameter types and a return type assignable to {@code returns}. Ambiguity means no match.
 */
public final class Sig {

    private Sig() {
    }

    public static Method find(Class<?> cls, Class<?> returns, Class<?>... params) {
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            Method found = null;
            for (Method m : c.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) || m.isSynthetic() || m.isBridge()) continue;
                if (!java.util.Arrays.equals(m.getParameterTypes(), params)) continue;
                if (returns != null && !returns.isAssignableFrom(m.getReturnType())) continue;
                if (found != null) return null;
                found = m;
            }
            if (found != null) {
                found.setAccessible(true);
                return found;
            }
        }
        return null;
    }

    /** Calls {@code name} if the object has it, else the method {@link #find} picks. */
    public static Object call(Object obj, String name, Class<?> returns, Class<?>[] params, Object... args)
            throws Throwable {
        try {
            Method m = XReflect.findMethodExactIfExists(obj.getClass(), name, params);
            if (m == null) m = find(obj.getClass(), returns, params);
            if (m == null) throw new NoSuchMethodException(obj.getClass().getName() + "#" + name);
            m.setAccessible(true);
            return m.invoke(obj, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
