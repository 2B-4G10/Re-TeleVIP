package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.messenger.MessageObject;
import com.my.televip.virtuals.messenger.MessagesController;
import com.my.televip.virtuals.messenger.UserConfig;
import com.my.televip.virtuals.tgnet.TLRPC;
import com.my.televip.virtuals.ui.SecretMediaViewer;
import com.my.televip.xposed.XBridge;

import com.my.televip.reflect.XReflect;
import com.my.televip.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreventMedia {

    public static boolean isEnable = false;

    /**
     * MessagesController.markMessageAsRead2, hooked so it never queues the local deletion. Opening
     * secret media tells the sender nothing; "Mark as seen" in the viewer calls this to tell them.
     */
    private static Method markAsRead;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                markAsRead = hookMarkAsRead();

                if (ClassLoad.getClass(ClassNames.CHAT_ACTIVITY) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "sendSecretMessageRead", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendSecretMessageRead", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT), boolean.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (!ConfigManager.preventMedia.isEnable()) return;
                            // Opening it reports nothing: the sender is told only on "Mark as seen".
                            param.setResult(null);
                            // readNow false: a viewer is about to open it. Current builds show it
                            // in PhotoViewer, older ones in SecretMediaViewer (which also calls this).
                            Object message = messageArg(param.args);
                            if (message != null && !readNowArg(param.args)) {
                                SecretMediaButtons.showFor(message, new MessageObject(message).getMessageOwner().getTtl());
                            }
                        }
                    }));
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "sendSecretMediaDelete", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendSecretMediaDelete", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT)}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.preventMedia.isEnable()) param.setResult(null);
                        }
                    }));
                }

                // sendSecretMessageRead and sendSecretMediaDelete are small private methods, and
                // ART compiles them into the chat's tap handlers (Telegram 13.1 on Android 15):
                // the hooks above then never run and an opened photo still expires. Running the
                // handlers interpreted makes them call the hooked methods.
                // Looked up quietly: builds that rename it (Nekogram) keep it under another name,
                // and a miss here is not worth the health report's "incompatible version" warning.
                Class<?> cellDelegate = XReflect.findClassIfExists(
                        AutomationResolver.resolve(ClassNames.CHAT_MESSAGE_CELL_DELEGATE), Utils.classLoader);
                if (cellDelegate != null) {
                    for (java.lang.reflect.Method method : cellDelegate.getDeclaredMethods()) {
                        XBridge.deoptimize(method);
                    }
                }

                if (ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER) != null) {
                    SecretMediaViewer.openMedia();
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER), AutomationResolver.resolve("SecretMediaViewer", "closePhoto", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("closePhoto", new Class<?>[]{boolean.class, boolean.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.preventMedia.isEnable()) {
                                // Belt and braces: the delete request is already blocked
                                // above, so a build where onClose is renamed loses nothing.
                                try {
                                    XReflect.setObjectField(param.thisObject, AutomationResolver.resolve("SecretMediaViewer", "onClose", AutomationResolver.ResolverType.Field), null);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    }));
                }

                if (markAsRead != null && ClassLoad.getClass(ClassNames.MESSAGES_STORAGE) != null) {
                    // Once the sender is told, the server lets the media expire, and the next
                    // history load would overwrite the copy kept here with the expired one.
                    SaveEditsHistory.hookTolerant(true,
                            ClassLoad.getClass(ClassNames.MESSAGES_STORAGE), AutomationResolver.resolve("MessagesStorage", "putMessages", AutomationResolver.ResolverType.Method), AutomationResolver.resolveObject("putMessages", new Class<?>[]{ClassLoad.getClass(ClassNames.TL_MESSAGES_MESSAGES), long.class, int.class, int.class, boolean.class, int.class, long.class}), new AbstractMethodHook() {
                                @Override
                                protected void beforeMethod(MethodHookParam param) {
                                    if (!ConfigManager.preventMedia.isEnable() || KeptSecretMedia.isEmpty()) return;
                                    Class<?> messagesClass = ClassLoad.getClass(ClassNames.TL_MESSAGES_MESSAGES);
                                    for (Object arg : param.args) {
                                        if (messagesClass != null && messagesClass.isInstance(arg)) {
                                            ArrayList<Object> messages = new TLRPC.messages_Messages(arg).getMessages();
                                            if (messages == null) return;
                                            for (int a = messages.size() - 1; a >= 0; a--) {
                                                TLRPC.Message message = new TLRPC.Message(messages.get(a));
                                                int mid = message.getID();
                                                if (KeptSecretMedia.containsMid(mid) && KeptSecretMedia.contains(dialogIdOf(message), mid)) {
                                                    messages.remove(a);
                                                }
                                            }
                                            return;
                                        }
                                    }
                                }
                            });
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

    /**
     * Tells the sender the media was opened, as opening it normally would. The timer then runs
     * out for them, while the copy here is kept.
     */
    public static boolean canMarkSeen() {
        return markAsRead != null;
    }

    public static boolean markSeen(long dialogId, int mid, int ttl) {
        if (markAsRead == null) return false;
        try {
            Object controller = MessagesController.getInstance(UserConfig.getSelectedAccount()).getMessagesController();
            // ttl only sizes the local deletion the hook drops, but 0 makes the client skip it all.
            markAsRead.invoke(controller, dialogId, mid, null, ttl > 0 ? ttl : Integer.MAX_VALUE, 0L, true);
            return true;
        } catch (Throwable t) {
            Logger.e(t);
            return false;
        }
    }

    /**
     * markMessageAsRead2 sends "opened" (messages.readMessageContents) and also queues the media's
     * deletion on this phone unless told not to. The hook keeps the request, drops the deletion,
     * and remembers the media as kept.
     */
    private static Method hookMarkAsRead() {
        Class<?> controller = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
        if (controller == null) return null;
        String name = AutomationResolver.resolve("MessagesController", "markMessageAsRead2", AutomationResolver.ResolverType.Method);
        // (dialogId, mid, inputChannel, ttl, taskId, createDeleteTask); the channel's class is
        // the one parameter not matched, so a renamed TLRPC class does not matter.
        Method found = null;
        for (Method method : controller.getDeclaredMethods()) {
            Class<?>[] p = method.getParameterTypes();
            if (method.getName().equals(name) && p.length == 6 && p[0] == long.class && p[1] == int.class
                    && p[3] == int.class && p[4] == long.class && p[5] == boolean.class) {
                found = method;
                break;
            }
        }
        if (found == null) {
            Logger.w("Prevent media: markMessageAsRead2 not found, no Mark as seen");
            return null;
        }
        HMethod.hookMethod(found, new AbstractMethodHook() {
            @Override
            protected void beforeMethod(MethodHookParam param) {
                if (ConfigManager.preventMedia.isEnable() && Boolean.TRUE.equals(param.args[5])) {
                    param.args[5] = false;
                    KeptSecretMedia.add((long) param.args[0], (int) param.args[1]);
                }
            }
        });
        found.setAccessible(true);
        return found;
    }

    /** The MessageObject argument, wherever R8 put it (it may make the method static). */
    private static Object messageArg(Object[] args) {
        Class<?> messageObject = ClassLoad.getClass(ClassNames.MESSAGE_OBJECT);
        for (Object arg : args) {
            if (messageObject != null && messageObject.isInstance(arg)) return arg;
        }
        return null;
    }

    private static boolean readNowArg(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Boolean) return (Boolean) arg;
        }
        return false;
    }

    /**
     * The chat of a message. Some renamed builds (Nekogram 12.10.5) map getDialogId to the wrong
     * method; secret media only comes in private chats, where the chat is the sender.
     */
    static long dialogIdOf(TLRPC.Message message) {
        try {
            return MessageObject.getDialogId(message);
        } catch (Throwable t) {
            return message.getFrom_id().getUser_id();
        }
    }

    static long dialogIdOf(Object messageObject) {
        MessageObject message = new MessageObject(messageObject);
        try {
            return message.getDialogId();
        } catch (Throwable t) {
            return message.getMessageOwner().getFrom_id().getUser_id();
        }
    }

    /** Messages of secret (end-to-end encrypted) chats have negative ids on the phone. */
    public static boolean isSecretChat(Object messageObject) {
        if (messageObject == null) return false;
        try {
            return new MessageObject(messageObject).getMessageOwner().getID() < 0;
        } catch (Throwable t) {
            return false;
        }
    }

}
