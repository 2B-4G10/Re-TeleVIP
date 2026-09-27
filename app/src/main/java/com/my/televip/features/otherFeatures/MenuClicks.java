package com.my.televip.features.otherFeatures;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Routes clicks on the menu items TeleVip adds to a fragment's action bar.
 *
 * <p>Works from the live objects rather than names: the fragment's action bar holds the
 * fragment's ActionBarMenuOnItemClick listener, and that listener's class overrides the one
 * {@code (int)void} method of the listener type. So it keeps working when R8 renames
 * onItemClick, the listener class and its this$0 field, or inlines setActionBarMenuOnItemClick.</p>
 */
final class MenuClicks {

    interface Handler {
        void onItemClick(Object fragment, int id);
    }

    private static final Set<Class<?>> HOOKED = new HashSet<>();

    private MenuClicks() {
    }

    /** Call once the fragment has created its action bar and set its listener. */
    static void attach(final Object fragment, final Handler handler) {
        try {
            Object actionBar = fieldOfType(fragment, ClassLoad.getClass(ClassNames.ACTION_BAR), "actionBar");
            Class<?> listenerType = ClassLoad.getClass(ClassNames.ACTION_BAR_MENU_ON_ITEM_CLICK);
            if (actionBar == null || listenerType == null) return;
            Object listener = fieldOfType(actionBar, listenerType, null);
            if (listener == null) return;

            final Class<?> listenerClass = listener.getClass();
            synchronized (HOOKED) {
                if (!HOOKED.add(listenerClass)) return;
            }
            Method click = overrideOf(listenerClass, clickMethod(listenerType));
            if (click == null) return;

            final Class<?> fragmentClass = fragment.getClass();
            HMethod.hookMethod(click.getDeclaringClass(), click.getName(), int.class, new AbstractMethodHook() {
                @Override
                protected void afterMethod(MethodHookParam param) {
                    try {
                        Object outer = outerOf(param.thisObject, fragmentClass);
                        if (outer != null) handler.onItemClick(outer, (int) param.args[0]);
                    } catch (Throwable t) {
                        Logger.e(t);
                    }
                }
            });
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /** The listener type's abstract onItemClick(int). */
    private static Method clickMethod(Class<?> listenerType) {
        for (Method m : listenerType.getDeclaredMethods()) {
            Class<?>[] p = m.getParameterTypes();
            if (m.getReturnType() == void.class && p.length == 1 && p[0] == int.class) return m;
        }
        return null;
    }

    private static Method overrideOf(Class<?> cls, Method abstractMethod) {
        if (abstractMethod == null) return null;
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(abstractMethod.getName(), int.class);
                if (!Modifier.isAbstract(m.getModifiers())) return m;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    /** The listener's enclosing fragment: whichever field holds an instance of the fragment class. */
    private static Object outerOf(Object listener, Class<?> fragmentClass) throws IllegalAccessException {
        for (Class<?> c = listener.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) continue;
                f.setAccessible(true);
                Object value = f.get(listener);
                if (fragmentClass.isInstance(value)) return value;
            }
        }
        return null;
    }

    /** A field of {@code obj} by name if it has one, else the first holding a {@code type}. */
    private static Object fieldOfType(Object obj, Class<?> type, String name) throws IllegalAccessException {
        for (Class<?> c = obj.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                boolean byName = name != null && f.getName().equals(name);
                boolean byType = type != null && type.isAssignableFrom(f.getType());
                if (!byName && !byType) continue;
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value != null && (type == null || type.isInstance(value))) return value;
            }
        }
        return null;
    }
}
