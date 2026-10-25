package com.my.televip.features;

import com.my.televip.Callback.IntCallback;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.application.AndroidUtilities;
import com.my.televip.Class.ClassLoad;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.SQLite.SQLiteCursor;
import com.my.televip.virtuals.messenger.MessageObject;
import com.my.televip.virtuals.messenger.MessagesController;
import com.my.televip.virtuals.messenger.MessagesStorage;
import com.my.televip.virtuals.messenger.UserConfig;
import com.my.televip.virtuals.messenger.Utilities;
import com.my.televip.virtuals.tgnet.ConnectionsManager;
import com.my.televip.virtuals.tgnet.RequestDelegate;
import com.my.televip.virtuals.tgnet.TLConstructor;
import com.my.televip.virtuals.tgnet.TLRPC;

import com.my.televip.reflect.XReflect;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.diagnostics.HookHealth;
import com.my.televip.diagnostics.OptionalSymbols;
import com.my.televip.hooks.HMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HideSeen {


    /** Mentions the chat has just cleared, which Hide seen lets the server hear about. */
    private static final Set<Integer> mentionReads = ConcurrentHashMap.newKeySet();
    /** TL_message's flag for "mentions you" (its mentioned field). */
    private static final int MESSAGE_MENTIONED = 1 << 4;

    /**
     * A mention on screen is marked seen with readMessageContents, which Hide seen blocked as a
     * read receipt: the "@" mark came back every time the chat was opened. Marking a text mention
     * seen tells the other members nothing, so those requests are let through.
     */
    public static void initMentions() {
        try {
            Class<?> controller = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
            Class<?> messageObject = ClassLoad.getClass(ClassNames.MESSAGE_OBJECT);
            if (controller == null || messageObject == null) return;
            String name = AutomationResolver.resolve("MessagesController", "markMessageContentAsRead", AutomationResolver.ResolverType.Method);
            Method found = XReflect.findMethodExactIfExists(controller, name, messageObject);
            if (found == null) {
                HookHealth.missingMember("MessagesController#markMessageContentAsRead");
                Logger.l("Not in this build: MessagesController#markMessageContentAsRead ("
                        + OptionalSymbols.reason("MessagesController#markMessageContentAsRead") + ")");
                return;
            }
            HMethod.hookMethod(found, new AbstractMethodHook() {
                @Override
                protected void beforeMethod(MethodHookParam param) {
                    if (!ConfigManager.hideSeen.isEnable() || param.args[0] == null) return;
                    try {
                        MessageObject message = new MessageObject(param.args[0]);
                        TLRPC.Message owner = message.getMessageOwner();
                        // A mention on screen; a played voice message still tells the sender.
                        if ((owner.getFlags() & MESSAGE_MENTIONED) != 0 && !message.isVoice()) {
                            mentionReads.add(owner.getID());
                        }
                    } catch (Throwable t) {
                        Logger.e(t);
                    }
                }
            });
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * Lets the read of these messages through Hide seen once: "Mark as seen" on secret media is
     * the user asking for the sender to be told.
     */
    public static void allowContentRead(int mid) {
        mentionReads.add(mid);
    }

    /** Whether a readMessageContents request only clears mentions the chat just cleared. */
    private static boolean clearsMentionsOnly(Object request) {
        if (mentionReads.isEmpty()) return false;
        for (Class<?> c = request.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.getType() != ArrayList.class || Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(request);
                    if (!(value instanceof ArrayList) || ((ArrayList<?>) value).isEmpty()) continue;
                    if (!mentionReads.containsAll((ArrayList<?>) value)) return false;
                    mentionReads.removeAll((ArrayList<?>) value);
                    return true;
                } catch (Throwable ignored) {
                }
            }
        }
        return false;
    }

    public static Object TLChannels_readHistory;
    public static Object TLMessages_readHistory;

    public static void sendFakeReadResponse(Object onCompleteOrig) {
        try {
            TLRPC.TL_messages_affectedMessages fakeRes = new TLRPC.TL_messages_affectedMessages();
            fakeRes.setPts(-1);
            fakeRes.setPtsCount(0);
            RequestDelegate onComplete = new RequestDelegate(onCompleteOrig);
            Utilities.getStageQueue().postRunnable(() -> {
                try {
                    if (onComplete.requestDelegate != null) {
                        onComplete.run(fakeRes.getTL_messages_affectedMessages(), null);
                    }
                } catch (Throwable e) {
                    Logger.e(e);
                }
            });
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    public static boolean isTLMessagesReadHistoryRequest(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
            return object.getClass().getName().contains("TL_messages_readHistory");
        } else {
            return TLConstructor.is(object, ClassNames.TL_MESSAGES_READ_HISTORY, TLConstructor.MESSAGES_READ_HISTORY);
        }
    }

    public static boolean isTLChannelsReadHistoryRequest(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
            return object.getClass().getName().contains("TL_channels_readHistory");
        } else {
            return TLConstructor.is(object, ClassNames.TL_CHANNELS_READ_HISTORY, TLConstructor.CHANNELS_READ_HISTORY);
        }
    }

    public static boolean isReadMessageRequest(Object object) {
        boolean privateHide = ConfigManager.hideSeenPrivateChat.isEnable();
        boolean channelHide = ConfigManager.hideSeenChannel.isEnable();

        boolean readHistory;
        boolean readDiscussion;
        boolean encryptedHistory;
        boolean readMessageContents;
        boolean channelReadMessageContents;
        boolean channelReadHistory;

        if (!ClientChecker.isTgnetObfuscated()) {
            String className = object.getClass().getName();

            readHistory = className.contains("TL_messages_readHistory");
            readDiscussion = className.contains("TL_messages_readDiscussion");
            encryptedHistory = className.contains("TL_messages_readEncryptedHistory");
            readMessageContents = className.contains("TL_messages_readMessageContents");
            channelReadMessageContents = className.contains("TL_channels_readMessageContents");
            channelReadHistory = className.contains("TL_channels_readHistory");
        } else {
            readHistory = TLConstructor.is(object, ClassNames.TL_MESSAGES_READ_HISTORY, TLConstructor.MESSAGES_READ_HISTORY);
            readDiscussion = TLConstructor.is(object, ClassNames.TL_MESSAGES_READ_DISCUSSION, TLConstructor.MESSAGES_READ_DISCUSSION);
            encryptedHistory = TLConstructor.is(object, ClassNames.TL_MESSAGES_READ_ENCRYPTED_HISTORY, TLConstructor.MESSAGES_READ_ENCRYPTED_HISTORY);
            readMessageContents = TLConstructor.is(object, ClassNames.TL_MESSAGES_READ_MESSAGE_CONTENTS, TLConstructor.MESSAGES_READ_MESSAGE_CONTENTS);
            channelReadMessageContents = TLConstructor.is(object, ClassNames.TL_CHANNELS_READ_MESSAGE_CONTENTS, TLConstructor.CHANNELS_READ_MESSAGE_CONTENTS);
            channelReadHistory = TLConstructor.is(object, ClassNames.TL_CHANNELS_READ_HISTORY, TLConstructor.CHANNELS_READ_HISTORY);
        }

        if ((readMessageContents || channelReadMessageContents) && clearsMentionsOnly(object)) {
            return false;
        }

        if (!(readHistory || readDiscussion ||
                (privateHide && encryptedHistory) ||
                (privateHide && readMessageContents) ||
                (channelHide && channelReadMessageContents) ||
                (channelHide && channelReadHistory))) {
            return false;
        }

        if (privateHide && channelHide) {
            return true;
        }

        if (readHistory || readDiscussion) {
            String objectName;
            if (!ClientChecker.isTgnetObfuscated()){
                objectName = object.getClass().getSimpleName();
            } else {
                if (readHistory)
                    objectName = "TLRPC$TL_messages_readHistory";
                else
                    objectName = "TLRPC$TL_messages_readDiscussion";
            }
            TLRPC.InputPeer inputPeer = new TLRPC.InputPeer(
                    XReflect.getObjectField(object, AutomationResolver.resolve(objectName,"peer", AutomationResolver.ResolverType.Field)));

            boolean isChannelOrGroup =
                    inputPeer.getChannel_id() > 0 ||
                            inputPeer.getChat_id() > 0;

            if (privateHide && !channelHide) {
                return !isChannelOrGroup;
            }

            if (channelHide && !privateHide) {
                return isChannelOrGroup;
            }

            return false;
        }

        return true;
    }

    public static void saveReadHistory(Object object) {
        if (HideSeen.TLChannels_readHistory == null && HideSeen.isTLChannelsReadHistoryRequest(object)) {
            HideSeen.TLChannels_readHistory = object;
        } else if (HideSeen.TLMessages_readHistory == null && HideSeen.isTLMessagesReadHistoryRequest(object)) {
            HideSeen.TLMessages_readHistory = object;
        }
    }

    public static void handleReadAfterSend(Object object) {
        try {
            if (ConfigManager.hideSeen.isEnable() && ConfigManager.markReadAfterSend.isEnable()) {
                TLRPC.InputPeer peer = extractPeerFromSendObject(object);

                if (peer != null && peer.inputPeer != null) {
                    Long dialogId = getDialogId(peer);
                    MessagesStorage messagesStorage = getMessagesStorage();
                    messagesStorage.getStorageQueue().postRunnable(() ->
                            getDialogMaxMessageId(messagesStorage, dialogId, (param -> markReadOnServer(param, peer))));
                }
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    public static void getDialogMaxMessageId(MessagesStorage messagesStorage, long dialog_id, IntCallback callback) {
        messagesStorage.getStorageQueue().postRunnable(() -> {
            SQLiteCursor cursor = null;
            int[] max = new int[1];
            try {
                cursor = messagesStorage.getDatabase().queryFinalized("SELECT MAX(mid) FROM messages_v2 WHERE uid = " + dialog_id, new Object[]{});
                if (cursor.next()) {
                    max[0] = cursor.intValue(0);
                }
            } catch (Throwable e) {
                Logger.e(e);
            } finally {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            AndroidUtilities.runOnUIThread(() -> callback.run(max[0]));
        });
    }

    public static boolean isReadMessages = false;

    public static void markReadOnServer(int messageId, TLRPC.InputPeer peer) {
        try {
            Object req;
            boolean inputPeerChannel;

            if (ClientChecker.isTgnetObfuscated()){
                inputPeerChannel = peer.inputPeer.getClass().getName().equals(AutomationResolver.resolve(ClassNames.TL_INPUT_PEER_CHANNEL));
            } else {
                inputPeerChannel = peer.inputPeer.getClass().getName().contains("TL_inputPeerChannel");
            }

            if (inputPeerChannel) {
                TLRPC.TL_channels_readHistory request;
                if (!ClientChecker.check(ClientChecker.ClientType.Nagram)) {
                    request = new TLRPC.TL_channels_readHistory();
                    request.setChannel(MessagesController.getInputChannel(peer));
                } else {
                    request = new TLRPC.TL_channels_readHistory(TLChannels_readHistory);
                    request.setChannel(MessagesController.getInputChannel(peer.getChannel_id()));
                }
                request.setMax_id(messageId);
                req = request.getTL_channels_readHistory();
            } else {
                TLRPC.TL_messages_readHistory request;
                if (!ClientChecker.check(ClientChecker.ClientType.Nagram)) {
                    request = new TLRPC.TL_messages_readHistory();
                } else {
                    request = new TLRPC.TL_messages_readHistory(TLMessages_readHistory);
                }
                request.setPeer(peer);
                request.setMax_id(messageId);
                req = request.getTL_messages_readHistory();
            }

            isReadMessages = true;
            getConnectionsManager().sendRequest(req, RequestDelegate.run((response, error) -> {
                if (error == null) {
                    if (ClassLoad.getClass(ClassNames.TL_MESSAGES_AFFECTED).isInstance(response)) {
                        TLRPC.TL_messages_affectedMessages res = new TLRPC.TL_messages_affectedMessages(response);
                        getMessagesController().processNewDifferenceParams(res.getPts(), -1, res.getPtsCount());
                    }

                }
            }));
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    private static TLRPC.InputPeer extractPeerFromSendObject(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
        String className = object.getClass().getName();
        if (className.contains("TL_messages_sendMessage") ||
                className.contains("TL_messages_sendMedia") ||
                className.contains("TL_messages_sendReaction") ||
                className.contains("TL_messages_sendPaidReaction") ||
                className.contains("TL_messages_sendMultiMedia"))
            return new TLRPC.InputPeer(getPeer(object));
        } else {
            if (sendRequestName(object) != null)
                return new TLRPC.InputPeer(getPeer(object));
        }
        return null;
    }

    /** The unrenamed name of a send request, by the constructor it sends; null for other requests. */
    private static String sendRequestName(Object request) {
        if (TLConstructor.is(request, ClassNames.TL_MESSAGES_SEND_MESSAGE, TLConstructor.MESSAGES_SEND_MESSAGE))
            return "TLRPC$TL_messages_sendMessage";
        if (TLConstructor.is(request, ClassNames.TL_MESSAGES_SEND_MEDIA, TLConstructor.MESSAGES_SEND_MEDIA))
            return "TLRPC$TL_messages_sendMedia";
        if (TLConstructor.is(request, ClassNames.TL_MESSAGES_SEND_REACTION, TLConstructor.MESSAGES_SEND_REACTION))
            return "TLRPC$TL_messages_sendReaction";
        if (TLConstructor.is(request, ClassNames.TL_MESSAGES_SEND_PAID_REACTION, TLConstructor.MESSAGES_SEND_PAID_REACTION))
            return "TLRPC$TL_messages_sendPaidReaction";
        if (TLConstructor.is(request, ClassNames.TL_MESSAGES_SEND_MULTI_MEDIA, TLConstructor.MESSAGES_SEND_MULTI_MEDIA))
            return "TLRPC$TL_messages_sendMultiMedia";
        return null;
    }

    private static Object getPeer(Object msg) {
        if (ClientChecker.isTgnetObfuscated()) {
            String msgName = sendRequestName(msg);
            return XReflect.getObjectField(msg, AutomationResolver.resolve(msgName, "peer", AutomationResolver.ResolverType.Field));
        } else {
            return XReflect.getObjectField(msg, "peer");
        }
    }

    public static Long getDialogId(TLRPC.InputPeer peer) {
        long dialogId;
        if (peer.getChat_id() != 0) {
            dialogId = -peer.getChat_id();
        } else if (peer.getChannel_id() != 0) {
            dialogId = -peer.getChannel_id();
        } else {
            dialogId = peer.getUser_id();
        }

        return dialogId;
    }

    public static MessagesStorage getMessagesStorage() {
        return MessagesStorage.getInstance(UserConfig.getSelectedAccount());
    }

    public static ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance(UserConfig.getSelectedAccount());
    }

    public static MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.getSelectedAccount());
    }

}
