package com.my.televip.features;

import android.os.SystemClock;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.diagnostics.HookHealth;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.tgnet.TLRPC;

import java.lang.reflect.Method;

/**
 * Clears the reactions button of a forum topic once its liked messages are seen.
 *
 * <p>The button asks MessagesController for the next message with unread reactions. When the
 * client has none saved for the topic, it asks the server - for the whole group, as the client
 * leaves out the topic (it only adds a monoforum's). The answer is a message of another topic, or
 * one the topic cannot show, so the count never reaches 0 and the badge stays. The same request
 * is sent with the topic here, as the official apps do: the server answers with the topic's
 * liked message, or none, and the client then marks the topic's reactions as read.</p>
 */
public class TopicReactions {

    private static final String REQUEST = "TLRPC$TL_messages_getUnreadReactions";

    public static boolean isEnable = false;

    /** The chat and topic the reactions button last asked about, and when. */
    private static volatile long dialogId, topicId, askedAt;

    private static volatile Class<?> request;

    public static void init() {
        if (isEnable) return;
        isEnable = true;
        try {
            Class<?> messagesController = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
            Class<?> connectionsManager = ClassLoad.getClass(ClassNames.CONNECTIONS_MANAGER);
            request = ClassLoad.getClass(ClassNames.TL_MESSAGES_GET_UNREAD_REACTIONS);
            if (messagesController == null || connectionsManager == null || request == null) return;

            Method next = nextReactionMention(messagesController);
            if (next == null) return;
            HMethod.hookMethod(next, new AbstractMethodHook() {
                @Override
                protected void beforeMethod(MethodHookParam param) {
                    // (dialogId, topicId, count, ...): builds that group parameters by type keep
                    // the two longs in this order.
                    long[] ids = new long[2];
                    int n = 0;
                    for (Object arg : param.args) {
                        if (arg instanceof Long && n < 2) ids[n++] = (Long) arg;
                    }
                    if (n < 2) return;
                    dialogId = ids[0];
                    topicId = ids[1];
                    askedAt = SystemClock.uptimeMillis();
                }
            });

            HMethod.hookMethod(connectionsManager,
                    AutomationResolver.resolve("ConnectionsManager", "sendRequestInternal", AutomationResolver.ResolverType.Method),
                    AutomationResolver.merge(AutomationResolver.resolveObject("sendRequestInternal", new Class<?>[]{ClassLoad.getClass(ClassNames.TL_OBJECT), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE), ClassLoad.getClass(ClassNames.REQUEST_DELEGATE_TIMESTAMP), ClassLoad.getClass(ClassNames.QUICK_ACK_DELEGATE), ClassLoad.getClass(ClassNames.WRITE_TO_SOCKET_DELEGATE), int.class, int.class, int.class, boolean.class, int.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            Object req = param.args[0];
                            if (req != null && req.getClass() == request) addTopic(req);
                        }
                    }));
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /** Puts the topic the button asked about into a request that left it out. */
    private static void addTopic(Object req) {
        try {
            long topic = topicId;
            // Topics are numbered by their first message's id; other chat modes pass other ids.
            if (topic <= 0 || topic > Integer.MAX_VALUE || SystemClock.uptimeMillis() - askedAt > 10_000) return;
            String flagsField = AutomationResolver.resolve(REQUEST, "flags", AutomationResolver.ResolverType.Field);
            int flags = XReflect.getIntField(req, flagsField);
            // Flag 0 is a topic, flag 1 a monoforum's chat: the client already said where.
            if ((flags & 3) != 0) return;
            Object peer = XReflect.getObjectField(req, AutomationResolver.resolve(REQUEST, "peer", AutomationResolver.ResolverType.Field));
            // Forum topics are in supergroups only, which is also what keeps saved messages and
            // quick replies (the user's own chat) out.
            if (peer == null) return;
            long channelId = new TLRPC.InputPeer(peer).getChannel_id();
            if (channelId == 0 || -channelId != dialogId) return;
            XReflect.setIntField(req, AutomationResolver.resolve(REQUEST, "top_msg_id", AutomationResolver.ResolverType.Field), (int) topic);
            XReflect.setIntField(req, flagsField, flags | 1);
            topicId = 0;
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * getNextReactionMention(dialogId, topicId, count, callback), or the method Telegram 13.x
     * moved its body to, which also takes whether it is about reactions or poll votes.
     */
    private static Method nextReactionMention(Class<?> messagesController) {
        String internal = AutomationResolver.resolve("MessagesController", "getNextReactionMentionInternal", AutomationResolver.ResolverType.Method);
        String plain = AutomationResolver.resolve("MessagesController", "getNextReactionMention", AutomationResolver.ResolverType.Method);
        Method found = withShape(messagesController, internal, true);
        if (found == null) found = withShape(messagesController, plain, false);
        if (found == null) {
            HookHealth.missingMember("MessagesController#getNextReactionMention");
            Logger.w("Topic reactions: no getNextReactionMention in this build");
            return null;
        }
        HookHealth.resolved();
        return found;
    }

    /** The one method of this name taking two longs, an int, an object and - if asked - a boolean. */
    private static Method withShape(Class<?> owner, String name, boolean withFlag) {
        Method found = null;
        for (Method m : owner.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getReturnType() != void.class || m.isSynthetic()) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length != (withFlag ? 5 : 4)) continue;
            int longs = 0, ints = 0, booleans = 0, objects = 0;
            for (Class<?> c : p) {
                if (c == long.class) longs++;
                else if (c == int.class) ints++;
                else if (c == boolean.class) booleans++;
                else if (!c.isPrimitive()) objects++;
            }
            if (longs != 2 || ints != 1 || objects != 1 || booleans != (withFlag ? 1 : 0)) continue;
            if (found != null) return null;
            found = m;
        }
        return found;
    }
}
