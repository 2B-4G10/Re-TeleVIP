package com.my.televip.virtuals.messenger;

import android.content.SharedPreferences;
import android.util.SparseArray;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.androidx.LongSparseArray;
import com.my.televip.virtuals.tgnet.TLRPC;

import com.my.televip.reflect.XReflect;

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

    public static MessagesController getInstance(int num){
        return new MessagesController(XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), AutomationResolver.resolve("MessagesController", "getInstance", AutomationResolver.ResolverType.Method), num));
    }
}
