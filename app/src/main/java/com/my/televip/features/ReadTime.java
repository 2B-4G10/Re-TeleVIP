package com.my.televip.features;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.application.ApplicationLoaderHook;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.tgnet.ConnectionsManager;
import com.my.televip.virtuals.tgnet.RequestDelegate;
import com.my.televip.virtuals.tgnet.TLRPC;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * When the other side read each of your messages in a private chat, put before its time:
 * "Read 12:41". Asked with messages.getOutboxReadDate, which only reads that date, so nothing is
 * marked as read and Ghost Mode is untouched. Telegram answers for the last seven days, and not
 * when either side hides their read time; those messages just keep their ticks.
 */
public final class ReadTime {

    private static final String GET_OUTBOX_READ_DATE = "org.telegram.tgnet.TLRPC$TL_messages_getOutboxReadDate";
    private static final String OUTBOX_READ_DATE = "org.telegram.tgnet.TLRPC$TL_outboxReadDate";

    /** TL_message's "out" flag: the account sent it. */
    private static final int MESSAGE_OUT = 1 << 1;
    /** How far back Telegram keeps read dates. */
    private static final int KEPT_SECONDS = 7 * 24 * 60 * 60;
    /** No date to show: asked and refused, or not asked because it can't be had. */
    private static final int NONE = -1;
    /** Asked, answer not back yet. */
    private static final int ASKED = 0;

    /** "account:dialog:message" to the date it was read, NONE or ASKED. */
    private static final Map<String, Integer> dates = new ConcurrentHashMap<>();

    /**
     * Your messages seen while still unread, to ask about once the other side reads them. Kept by
     * the message itself: one just sent only gets its real id once the server has it.
     */
    private static final Map<Object, Boolean> pending = Collections.synchronizedMap(new WeakHashMap<>());
    private static final int MAX_PENDING = 200;

    private static final Handler main = new Handler(Looper.getMainLooper());
    private static boolean refreshPosted;
    private static boolean isEnable;

    private ReadTime() {
    }

    /**
     * A message read while its chat is showing is not measured again when its ticks turn, so
     * the client's "messages read" event is watched: then the waiting messages are asked about.
     */
    public static void init() {
        if (isEnable || !isSupported()) return;
        isEnable = true;
        try {
            final Class<?> center = ClassLoad.getClass(ClassNames.NOTIFICATION_CENTER);
            final int messagesRead = XReflect.getStaticIntField(center, "messagesRead");
            HMethod.hookMethod(center, AutomationResolver.resolve("NotificationCenter", "postNotificationName", AutomationResolver.ResolverType.Method),
                    AutomationResolver.merge(AutomationResolver.resolveObject("postNotificationName", new Class<?>[]{int.class, Object[].class}), new AbstractMethodHook() {
                        @Override
                        protected void afterMethod(MethodHookParam param) {
                            if ((int) param.args[0] == messagesRead && !pending.isEmpty()
                                    && ConfigManager.showReadTime != null && ConfigManager.showReadTime.isEnable()) {
                                // After the chat has marked its messages read.
                                main.postDelayed(ReadTime::askRead, 300);
                            }
                        }
                    }));
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /** Asks about each waiting message that is read now; the rest keep waiting. */
    private static void askRead() {
        Object[] waiting;
        synchronized (pending) {
            waiting = pending.keySet().toArray();
        }
        for (Object messageObject : waiting) {
            if (messageObject == null) continue;
            try {
                TLRPC.Message owner = new TLRPC.Message(XReflect.getObjectField(messageObject, "messageOwner"));
                if (Boolean.TRUE.equals(XReflect.getObjectField(owner.message, "unread"))) continue;
                pending.remove(messageObject);
                String key = keyOf(messageObject, owner);
                if (key == null || dates.containsKey(key)) continue;
                dates.put(key, ASKED);
                ask(XReflect.getIntField(messageObject, "currentAccount"), owner.getPeer_id().getUser_id(), owner.getID(), key, messageObject);
            } catch (Throwable t) {
                pending.remove(messageObject);
                Logger.e(t);
            }
        }
    }

    /** Whether this client's request classes have their real names; the request is made by name. */
    public static boolean isSupported() {
        return !ClientChecker.isTgnetObfuscated();
    }

    /** "account:user:message" for a message of yours in a private chat, else null. */
    private static String keyOf(Object messageObject, TLRPC.Message owner) {
        if ((owner.getFlags() & MESSAGE_OUT) == 0) return null;
        TLRPC.Peer peer = owner.getPeer_id();
        if (peer == null || peer.isEmpty() || peer.getUser_id() == 0 || owner.getID() <= 0) return null;
        return XReflect.getIntField(messageObject, "currentAccount") + ":" + peer.getUser_id() + ":" + owner.getID();
    }

    /**
     * The label to put before the time of {@code messageObject}, or null for none. The first time
     * a message is seen the date is asked for, and the chat is drawn again once it is back.
     */
    public static String label(Object messageObject, TLRPC.Message owner) {
        if (ConfigManager.showReadTime == null || !ConfigManager.showReadTime.isEnable()) return null;
        try {
            if ((owner.getFlags() & MESSAGE_OUT) == 0) return null;
            TLRPC.Peer peer = owner.getPeer_id();
            if (peer == null || peer.isEmpty() || peer.getUser_id() == 0) return null;
            if (Boolean.TRUE.equals(XReflect.getObjectField(owner.message, "unread"))) {
                if (pending.size() >= MAX_PENDING) pending.clear();
                pending.put(messageObject, Boolean.TRUE);
                return null;
            }
            String key = keyOf(messageObject, owner);
            if (key == null) return null;
            int id = owner.getID();
            int account = XReflect.getIntField(messageObject, "currentAccount");
            Integer date = dates.get(key);
            if (date == null) {
                int sent = XReflect.getIntField(owner.message, "date");
                if (System.currentTimeMillis() / 1000 - sent > KEPT_SECONDS) {
                    dates.put(key, NONE);
                    return null;
                }
                dates.put(key, ASKED);
                ask(account, peer.getUser_id(), id, key, messageObject);
                return null;
            }
            if (date <= 0) return null;
            int sent = XReflect.getIntField(owner.message, "date");
            return Translator.get(Keys.ReadAt, format(date, sent));
        } catch (Throwable t) {
            Logger.e(t);
            return null;
        }
    }

    private static void ask(int account, long userId, int messageId, String key, Object messageObject) {
        try {
            Class<?> requestClass = ClassLoad.getClass(GET_OUTBOX_READ_DATE);
            Object messagesController = XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), "getInstance", account);
            Object inputPeer = XReflect.callMethod(messagesController, "getInputPeer", userId);
            if (requestClass == null || inputPeer == null) {
                dates.put(key, NONE);
                return;
            }
            Object request = requestClass.getDeclaredConstructor().newInstance();
            XReflect.setObjectField(request, "peer", inputPeer);
            XReflect.setIntField(request, "msg_id", messageId);
            final Class<?> answerClass = ClassLoad.getClass(OUTBOX_READ_DATE);
            ConnectionsManager.getInstance(account).sendRequest(request, RequestDelegate.run((response, error) -> {
                if (error == null && answerClass != null && answerClass.isInstance(response)) {
                    dates.put(key, XReflect.getIntField(response, "date"));
                    refresh(account, messageObject);
                } else {
                    dates.put(key, NONE);
                }
            }));
        } catch (Throwable t) {
            dates.put(key, NONE);
            Logger.e(t);
        }
    }

    /**
     * Has the open chat draw its messages again, once for a burst of answers: the message is
     * marked to be measured anew, and an interface update makes the chat rebind what it shows.
     */
    private static void refresh(int account, Object messageObject) {
        main.post(() -> {
            try {
                XReflect.setBooleanField(messageObject, "forceUpdate", true);
            } catch (Throwable ignored) {
                // Not in this build: the label shows the next time the message is drawn.
            }
            if (refreshPosted) return;
            refreshPosted = true;
            main.postDelayed(() -> {
                refreshPosted = false;
                try {
                    Class<?> center = ClassLoad.getClass(ClassNames.NOTIFICATION_CENTER);
                    Object instance = XReflect.callStaticMethod(center, "getInstance", account);
                    int updateInterfaces = XReflect.getStaticIntField(center, "updateInterfaces");
                    // MessagesController.UPDATE_MASK_AVATAR: the chat redraws its rows for it.
                    XReflect.callMethod(instance, "postNotificationName", updateInterfaces, new Object[]{2});
                } catch (Throwable t) {
                    Logger.e(t);
                }
            }, 300);
        });
    }

    /** The read time as the phone writes times, with the day too when it isn't the day it was sent. */
    private static String format(int read, int sent) {
        Context context = ApplicationLoaderHook.getApplicationContext();
        long readMs = read * 1000L;
        int flags = DateUtils.FORMAT_SHOW_TIME;
        if (!sameDay(readMs, sent * 1000L)) {
            flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR;
        }
        return DateUtils.formatDateTime(context, readMs, flags);
    }

    private static boolean sameDay(long a, long b) {
        Calendar first = Calendar.getInstance(), second = Calendar.getInstance();
        first.setTimeInMillis(a);
        second.setTimeInMillis(b);
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }
}
