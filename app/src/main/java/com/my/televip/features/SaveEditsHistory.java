package com.my.televip.features;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.Database.MessageDatabase;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.calendar.ConverterCalendar;
import com.my.televip.diagnostics.HookHealth;
import com.my.televip.hooks.HMethod;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.SQLite.SQLiteCursor;
import com.my.televip.virtuals.SQLite.SQLiteDatabase;
import com.my.televip.virtuals.SettingsIconResolver;
import com.my.televip.virtuals.Theme;
import com.my.televip.virtuals.messenger.BaseController;
import com.my.televip.virtuals.messenger.MessageObject;
import com.my.televip.virtuals.messenger.MessagesStorage;
import com.my.televip.virtuals.messenger.UserConfig;
import com.my.televip.virtuals.tgnet.NativeByteBuffer;
import com.my.televip.virtuals.tgnet.TLRPC;
import com.my.televip.virtuals.ui.BaseFragment;
import com.my.televip.virtuals.ui.ChatActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

public class SaveEditsHistory {

    private static MessageDatabase messageDatabase;

    public static boolean isEnable = false;

    public static void init(Context context) {
        try {
            if (!isEnable) {
                isEnable = true;
                messageDatabase = MessageDatabase.get(context);

                if (ClassLoad.getClass(ClassNames.CHAT_ACTIVITY) != null) {

                    hookTolerant(false,
                            ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "fillMessageMenu", AutomationResolver.ResolverType.Method), AutomationResolver.resolveObject("fillMessageMenu", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT), ArrayList.class, ArrayList.class, ArrayList.class}), new AbstractMethodHook() {
                                @Override
                                @SuppressWarnings("unchecked")   // the client's own lists, changed in place
                                protected void afterMethod(MethodHookParam param) {
                                    if (ConfigManager.saveEditsHistory.isEnable()) {
                                        ChatActivity chatActivity = new ChatActivity(param.thisObject);

                                        if (chatActivity.getSelectedObject() != null) {
                                            MessageObject messageObject = chatActivity.getSelectedObject();

                                            if (messageObject.getMessageOwner() != null) {

                                                TLRPC.Message message = messageObject.getMessageOwner();
                                                if (message != null && message.getID() > 0) {
                                                    long account = accountOf(param.thisObject);
                                                    long dialogId = getDialogId(message);

                                                    if (dialogId != 0 && messageDatabase.getMessage(account, dialogId, message.getID()) != null) {

                                                        ArrayList<Integer> icons;
                                                        ArrayList<CharSequence> items;
                                                        ArrayList<Integer> options;

                                                        if (ClientChecker.check(ClientChecker.ClientType.Telegraph)) {
                                                            icons = (ArrayList<Integer>) param.args[2];
                                                            items = (ArrayList<CharSequence>) param.args[3];
                                                            options = (ArrayList<Integer>) param.args[4];
                                                        } else {
                                                            icons = (ArrayList<Integer>) param.args[1];
                                                            items = (ArrayList<CharSequence>) param.args[2];
                                                            options = (ArrayList<Integer>) param.args[3];
                                                        }

                                                        items.add(Translator.get(Keys.EditsHistory));
                                                        options.add(8353847);
                                                        if (!ClientChecker.check(ClientChecker.ClientType.Nagram))
                                                            icons.add(SettingsIconResolver.getIconSettings(context));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                    HMethod.hookMethod(
                            ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "processSelectedOption", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("processSelectedOption", new Class<?>[]{int.class}), new AbstractMethodHook() {
                                @Override
                                protected void beforeMethod(MethodHookParam param) {
                                    if (ConfigManager.saveEditsHistory.isEnable()) {
                                        int option = (int) param.args[0];
                                        ChatActivity chatActivity = new ChatActivity(param.thisObject);

                                        if (option == 8353847) {

                                            if (chatActivity.getSelectedObject() != null) {
                                                MessageObject messageObject = chatActivity.getSelectedObject();

                                                if (messageObject.getMessageOwner() != null) {

                                                    TLRPC.Message message = messageObject.getMessageOwner();
                                                    long account = accountOf(param.thisObject);
                                                    long dialogId = getDialogId(message);
                                                    if (message.message != null && message.getID() > 0 && message.getMessage() != null) {

                                                        if (dialogId != 0 && messageDatabase.searchMessage(account, dialogId, message.getID())) {

                                                            AlertDialog alertDialog = new AlertDialog(context);
                                                            alertDialog.setTitle(Translator.get(Keys.EditsHistory));

                                                            TextView textView = new TextView(context);

                                                            int maxMsgCount = messageDatabase.getMaxMessageCount(account, dialogId, message.getID());

                                                            StringBuilder builder = new StringBuilder();

                                                            if (maxMsgCount > 1) {

                                                                for (int i = 1; i <= maxMsgCount; i++) {

                                                                    String msg = messageDatabase.getMessage(account, dialogId, message.getID(), i);

                                                                    if (msg != null) {

                                                                        long messageDate = messageDatabase.getMessageDate(account, dialogId, message.getID(), i);

                                                                        if (messageDate != 0) {
                                                                            String date = ConverterCalendar.formatDate(messageDate);

                                                                            builder.append(Translator.get(Keys.Message)).append(i).append(" ").append(date).append("\n");
                                                                            builder.append(msg).append("\n");
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                long messageDate = messageDatabase.getMessageDate(account, dialogId, message.getID(), 1);

                                                                if (messageDate != 0) {
                                                                    String date = ConverterCalendar.formatDate(messageDate);

                                                                    builder.append(date).append("\n");
                                                                }
                                                                builder.append(messageDatabase.getMessage(account, dialogId, message.getID()));
                                                            }

                                                            textView.setText(builder.toString());
                                                            textView.setPadding(32, 32, 32, 32);
                                                            textView.setTextSize(16);
                                                            textView.setTextColor(Theme.getTextColor());
                                                            textView.setMovementMethod(new ScrollingMovementMethod());
                                                            textView.setTextIsSelectable(true);

                                                            ScrollView scrollView = new ScrollView(context);
                                                            scrollView.addView(textView);
                                                            alertDialog.setView(scrollView);
                                                            alertDialog.setPositiveButton(Translator.get(Keys.Done), null);
                                                            alertDialog.show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }));
                }

                if (ClassLoad.getClass(ClassNames.MESSAGES_STORAGE) != null) {
                    hookTolerant(true,
                            ClassLoad.getClass(ClassNames.MESSAGES_STORAGE), AutomationResolver.resolve("MessagesStorage", "putMessages", AutomationResolver.ResolverType.Method), AutomationResolver.resolveObject("putMessages", new Class<?>[]{ClassLoad.getClass(ClassNames.TL_MESSAGES_MESSAGES), long.class, int.class, int.class, boolean.class, int.class, long.class}), new AbstractMethodHook() {
                                @Override
                                protected void beforeMethod(MethodHookParam param) {
                                    if (ConfigManager.saveEditsHistory.isEnable()) {
                                        // R8 groups the parameters by type in some builds (Nagram, Nekogram 12.10.5+),
                                        // keeping their order within a type: load_type is still the first int.
                                        Integer load_type = null;
                                        Object messagesObject = null;
                                        Class<?> messagesClass = ClassLoad.getClass(ClassNames.TL_MESSAGES_MESSAGES);
                                        for (Object arg : param.args) {
                                            if (load_type == null && arg instanceof Integer) load_type = (Integer) arg;
                                            else if (messagesObject == null && messagesClass != null && messagesClass.isInstance(arg)) messagesObject = arg;
                                        }

                                        if (load_type != null && load_type == -2) {
                                            Object messagesStorageObject = param.thisObject;

                                            if (messagesObject != null) {
                                                // Read on the storage queue, just ahead of the write the client
                                                // queues next, and never from the caller's (often the main)
                                                // thread, where it raced the client's own database work.
                                                final Object edited = messagesObject;
                                                MessagesStorage messagesStorage = new MessagesStorage(messagesStorageObject);
                                                messagesStorage.getStorageQueue().postRunnable(() -> saveOldTexts(messagesStorageObject, edited));
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /** Keeps the stored text of each edited message that the edit changes. */
    private static void saveOldTexts(Object messagesStorageObject, Object messagesObject) {
        try {
            MessagesStorage messagesStorage = new MessagesStorage(messagesStorageObject);
            TLRPC.messages_Messages messages = new TLRPC.messages_Messages(messagesObject);
            long clientUserId = new BaseController(messagesStorageObject).getUserConfig().getClientUserId();
            SQLiteDatabase sqLiteDatabase = messagesStorage.getDatabase();
            ArrayList<Object> list = messages.getMessages();
            for (int a = 0, count = list.size(); a < count; a++) {
                TLRPC.Message message = new TLRPC.Message(list.get(a));
                SQLiteCursor cursor = null;
                try {
                    cursor = sqLiteDatabase.queryFinalized(String.format(Locale.US, "SELECT data FROM messages_v2 WHERE mid = %d AND uid = %d", message.getID(), MessageObject.getDialogId(message)), new Object[]{});
                    if (!cursor.next()) continue;
                    NativeByteBuffer data = cursor.byteBufferValue(0);
                    if (data.nativeByteBuffer == null) continue;
                    TLRPC.Message oldMessage = TLRPC.Message.TLdeserialize(data, data.readInt32(false), false);
                    oldMessage.readAttachPath(data, clientUserId);
                    data.reuse();
                    long dialogId = getDialogId(message);
                    if (oldMessage.getMessage() != null && message.getMessage() != null
                            && !oldMessage.getMessage().equals(message.getMessage()) && dialogId != 0) {
                        messageDatabase.addMessage(clientUserId, dialogId, oldMessage.getID(), oldMessage.getMessage());
                    }
                } catch (Throwable t) {
                    Logger.e(t);
                } finally {
                    // A cursor left open keeps its statement, and the read it holds, alive.
                    if (cursor != null) cursor.dispose();
                }
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * Hooks {@code name} with these parameters, or - when a build has changed them - the one
     * method of that name taking them in another order ({@code anyOrder}, for a hook that reads
     * its arguments by type) or followed by more (a fork's extra parameter: Momogram's
     * fillMessageMenu). Anything else is left to the usual lookup, which reports it.
     */
    static void hookTolerant(boolean anyOrder, Class<?> owner, String name, Class<?>[] params, AbstractMethodHook hook) {
        if (owner == null) return;
        Method method = XReflect.findMethodExactIfExists(owner, name, params);
        if (method == null) {
            method = anyOrder ? XReflect.findMethodAnyOrderIfExists(owner, name, params)
                    : XReflect.findMethodWithMoreParamsIfExists(owner, name, params);
            if (method != null) Logger.l("signature differs, hooking " + method);
        }
        if (method != null) {
            HMethod.hookMethod(method, hook);
            HookHealth.resolved();
        } else {
            HMethod.hookMethod(owner, name, AutomationResolver.merge(params, hook));
        }
    }

    /** The user id of the account whose chat is open, so two accounts' histories never mix. */
    private static long accountOf(Object chatActivity) {
        try {
            return new BaseFragment(chatActivity).getUserConfig().getClientUserId();
        } catch (Throwable t) {
            Logger.e(t);
            return 0;
        }
    }

    /** TL_message's "out" flag: the account sent it. */
    private static final int MESSAGE_OUT = 1 << 1;

    /**
     * The chat a message is in, as the client numbers dialogs: a user's id for a private chat,
     * minus the group's or channel's id for the others. Message ids are only unique within that
     * chat (and, for private chats and small groups, the account), so history is kept per chat.
     * Worked out from the message itself: MessageObject.getDialogId is mis-mapped on some renamed
     * builds (Nekogram 12.10.5).
     */
    private static long getDialogId(TLRPC.Message message) {
        try {
            TLRPC.Peer peer = message.getPeer_id();
            if (!peer.isEmpty()) {
                if (peer.getChat_id() != 0) return -peer.getChat_id();
                if (peer.getChannel_id() != 0) return -peer.getChannel_id();
                if (peer.getUser_id() != 0) {
                    if ((message.getFlags() & MESSAGE_OUT) != 0) return peer.getUser_id();
                    long sender = getDialogId(message.getFrom_id());
                    return sender != 0 ? sender : peer.getUser_id();
                }
            }
        } catch (Throwable t) {
            // A build where peer_id is not mapped: the sender still tells most chats apart.
        }
        try {
            return getDialogId(message.getFrom_id());
        } catch (Throwable t) {
            return 0;
        }
    }

    /** A user's id, or minus a group's or channel's id. */
    private static long getDialogId(TLRPC.Peer peer) {
        if (peer == null || peer.isEmpty()) return 0;
        if (peer.getUser_id() != 0) return peer.getUser_id();
        if (peer.getChat_id() != 0) return -peer.getChat_id();
        if (peer.getChannel_id() != 0) return -peer.getChannel_id();
        return 0;
    }

}
