package com.my.televip.virtuals.messenger;

import android.content.SharedPreferences;
import android.util.SparseArray;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.androidx.LongSparseArray;
import com.my.televip.virtuals.tgnet.TLRPC;

import com.my.televip.logging.Logger;
import com.my.televip.reflect.XReflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class MessagesController {
    final Object messagesController;

    public MessagesController(Object instance)
    {
        this.messagesController = instance;
    }

    /** Applies a pts update the module caused itself (reading history), as the client would. */
    public void processNewDifferenceParams(int pts, int date, int pts_count) {
        String full = AutomationResolver.resolve("MessagesController", "processNewDifferenceParams", AutomationResolver.ResolverType.Method);
        try {
            XReflect.callMethod(messagesController, full, -1, pts, date, pts_count);
        } catch (Throwable withoutSeq) {
            // seq only ever reached the log; Nagram and Nekogram 12.10.5+ dropped it.
            XReflect.callMethod(messagesController, AutomationResolver.resolveOverload("MessagesController",
                    "processNewDifferenceParamsIII", "processNewDifferenceParams"), pts, date, pts_count);
        }
    }

    public void removePromoDialog() {
        XReflect.callMethod(messagesController, AutomationResolver.resolve("MessagesController", "removePromoDialog", AutomationResolver.ResolverType.Method));
    }

    public static Object getInputChannel(TLRPC.InputPeer peer) {
        try {
            return XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER),
                    AutomationResolver.resolveOverload("MessagesController", "getInputChannelO2", "getInputChannel"), peer.inputPeer);
        } catch (Throwable inlined) {
            // R8 inlines the InputPeer overload where nothing else calls it (Nekogram 12.10.5+);
            // the account's lookup by channel id gives the same input channel.
            return getInputChannel(peer.getChannel_id());
        }
    }

    public SparseArray<Object> getDialogMessagesByIds() {
        return (SparseArray<Object>) XReflect.getObjectField(messagesController, AutomationResolver.resolve("MessagesController", "dialogMessagesByIds", AutomationResolver.ResolverType.Field));
    }

    public LongSparseArray getDialogMessage() {
        return  new LongSparseArray(XReflect.getObjectField(messagesController, AutomationResolver.resolve("MessagesController", "dialogMessage", AutomationResolver.ResolverType.Field)));
    }

    public static SharedPreferences getGlobalMainSettings() {
        return (SharedPreferences) XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), AutomationResolver.resolve("MessagesController", "getGlobalMainSettings", AutomationResolver.ResolverType.Method));
    }

    /** The input channel for a channel id (positive), from the selected account. */
    public static Object getInputChannel(long channelId) {
        // An instance method: it looks the chat up in this account's cache.
        return XReflect.callMethod(getInstance(UserConfig.getSelectedAccount()).messagesController,
                AutomationResolver.resolveOverload("MessagesController", "getInputChannelJ", "getInputChannel"), channelId);
    }

    public MessagesStorage getMessagesStorage() {
        return new MessagesStorage(XReflect.callMethod(messagesController, AutomationResolver.resolve("MessagesController", "getMessagesStorage", AutomationResolver.ResolverType.Method)));
    }

    /**
     * Deletes messages of one chat, for everyone when {@code forAll}, through the same overload
     * Show deleted messages hooks: (messages, randoms, encryptedChat, dialogId, forAll, mode,
     * cacheOnly, taskId, taskRequest, topicId, movedToScheduled, movedToScheduledMessageId), or
     * Momogram's with a completion callback after them. Not for secret chats (they need randoms).
     */
    public boolean deleteMessages(ArrayList<Integer> ids, long dialogId, boolean forAll) {
        Class<?> owner = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
        Class<?>[] params = AutomationResolver.resolveObject("deleteMessages", new Class<?>[]{ArrayList.class,
                ArrayList.class, ClassLoad.getClass(ClassNames.TLRPC_ENCRYPTED_CHAT), long.class, boolean.class, int.class,
                boolean.class, long.class, ClassLoad.getClass(ClassNames.TL_OBJECT), int.class, boolean.class, int.class});
        if (params.length != 12) return false;   // a build that reshaped it; the arguments would not fit
        Object[] args = {ids, null, null, dialogId, forAll, 0, false, 0L, null, 0, false, 0};
        try {
            Method method = XReflect.findMethodExactIfExists(owner,
                    AutomationResolver.resolve("MessagesController", "deleteMessages", AutomationResolver.ResolverType.Method), params);
            if (method == null) {
                Class<?>[] withCallback = Arrays.copyOf(params, params.length + 1);
                withCallback[params.length] = BiConsumer.class;
                method = XReflect.findMethodExactIfExists(owner,
                        AutomationResolver.resolveOverload("MessagesController", "deleteMessagesAAOJZIZJOIZIB", "deleteMessages"), withCallback);
                args = Arrays.copyOf(args, args.length + 1);
            }
            if (method == null) return false;
            method.setAccessible(true);
            method.invoke(messagesController, args);
            return true;
        } catch (Throwable t) {
            Logger.e(t);
            return false;
        }
    }

    public static MessagesController getInstance(int num){
        return new MessagesController(XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), AutomationResolver.resolve("MessagesController", "getInstance", AutomationResolver.ResolverType.Method), num));
    }
}
