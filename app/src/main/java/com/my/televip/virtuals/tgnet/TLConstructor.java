package com.my.televip.virtuals.tgnet;

import com.my.televip.Class.ClassLoad;
import com.my.televip.ClientChecker;
import com.my.televip.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tells a request by the constructor id it sends, not only by its class.
 *
 * <p>R8 merges small request classes in renamed builds (Nekogram 13.0.2 keeps
 * channels.readMessageContents and channels.getMessages in one class, told apart by a byte
 * field). The class that was found for one request then also stands for the others it was merged
 * with, and treating them all as that request drops requests the chat needs: blocking
 * channels.getMessages as a read made linked posts show "post was removed".</p>
 */
public final class TLConstructor {

    public static final int MESSAGES_READ_HISTORY = 0x0e306d3a;
    public static final int MESSAGES_READ_DISCUSSION = 0xf731a9f4;
    public static final int MESSAGES_READ_ENCRYPTED_HISTORY = 0x7f4b690a;
    public static final int MESSAGES_READ_MESSAGE_CONTENTS = 0x36a73f77;
    public static final int CHANNELS_READ_MESSAGE_CONTENTS = 0xeab5dc38;
    public static final int CHANNELS_READ_HISTORY = 0xcc104937;
    public static final int MESSAGES_SET_TYPING = 0x58943ee2;
    public static final int MESSAGES_SET_ENCRYPTED_TYPING = 0x791451ed;
    public static final int STORIES_READ_STORIES = 0xa556dac8;
    public static final int STORIES_INCREMENT_STORY_VIEWS = 0xb2028afb;
    public static final int ACCOUNT_UPDATE_STATUS = 0x6628562c;
    public static final int MESSAGES_SEND_MESSAGE = 0xfef48f62;
    public static final int MESSAGES_SEND_MEDIA = 0x0330e77f;
    public static final int MESSAGES_SEND_MULTI_MEDIA = 0x1bf89d74;
    public static final int MESSAGES_SEND_REACTION = 0xd30d78d4;
    public static final int MESSAGES_SEND_PAID_REACTION = 0x58bbcb50;
    public static final int MESSAGES_GET_SPONSORED_MESSAGES = 0x3d6ce850;
    public static final int CONTACTS_GET_SPONSORED_PEERS = 0xb6c8c393;
    public static final int HELP_GET_PROMO_DATA = 0xc0977421;

    private static final String OUTPUT_SERIALIZED_DATA = "org.telegram.tgnet.OutputSerializedData";

    /** serializeToStream of each request class, or a marker for classes that have none. */
    private static final Map<Class<?>, Method> serializers = new ConcurrentHashMap<>();
    private static final Method NONE;

    static {
        Method none = null;
        try {
            none = Object.class.getDeclaredMethod("toString");
        } catch (NoSuchMethodException ignored) {
        }
        NONE = none;
    }

    private static volatile Object stream;
    private static volatile boolean unavailable;

    private TLConstructor() {
    }

    /** Stops serializing once the constructor id is written. */
    private static final class Stop extends RuntimeException {
        final Integer constructor;

        Stop(Integer constructor) {
            super(null, null, false, false);
            this.constructor = constructor;
        }
    }

    /**
     * Whether {@code request} is of the class found for {@code className} and, in builds where that
     * class may stand for several requests, sends {@code constructor}.
     */
    public static boolean is(Object request, String className, int constructor) {
        if (request == null) return false;
        Class<?> type = ClassLoad.getClass(className);
        if (type == null || request.getClass() != type) return false;
        if (!ClientChecker.isTgnetObfuscated()) return true;
        Integer sent = of(request);
        // Builds whose stream type was not found keep the class check alone, as before.
        return sent == null || sent == constructor;
    }

    /** The constructor id {@code tl} writes first, or null when it cannot be read. */
    public static Integer of(Object tl) {
        if (unavailable) return null;
        try {
            Method serialize = serializer(tl.getClass());
            if (serialize == null) return null;
            serialize.invoke(tl, stream);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Stop) return ((Stop) e.getCause()).constructor;
            return null;
        } catch (Throwable t) {
            Logger.e(t);
        }
        return null;
    }

    private static Method serializer(Class<?> type) {
        Method cached = serializers.get(type);
        if (cached != null) return cached == NONE ? null : cached;
        Class<?> output = ClassLoad.getClass(OUTPUT_SERIALIZED_DATA);
        if (output == null || !output.isInterface()) {
            unavailable = true;
            return null;
        }
        if (stream == null) {
            stream = Proxy.newProxyInstance(output.getClassLoader(), new Class<?>[]{output}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    switch (method.getName()) {
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: return "TeleVipConstructorProbe";
                    }
                }
                // The first value a TL object writes is its constructor id.
                throw new Stop(args != null && args.length == 1 && args[0] instanceof Integer ? (Integer) args[0] : null);
            });
        }
        Method found = null;
        for (Class<?> c = type; c != null && c != Object.class && found == null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 1 && p[0] == output && m.getReturnType() == void.class
                        && !Modifier.isStatic(m.getModifiers())) {
                    found = m;
                    break;
                }
            }
        }
        if (found != null) found.setAccessible(true);
        serializers.put(type, found != null ? found : NONE);
        return found;
    }
}
