package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.messenger.MessageObject;
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
     * True once markMessageAsRead2 is hooked: Telegram may then tell the sender a secret media was
     * opened, because the hook keeps it from also queueing the local deletion. Without the hook
     * nothing is sent, as before.
     */
    public static boolean keepsLocalCopy = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                keepsLocalCopy = hookMarkAsRead();

                if (ClassLoad.getClass(ClassNames.CHAT_ACTIVITY) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "sendSecretMessageRead", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("sendSecretMessageRead", new Class<?>[]{ClassLoad.getClass(ClassNames.MESSAGE_OBJECT), boolean.class}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (!ConfigManager.preventMedia.isEnable()) return;
                            // Secret chats keep their media by not reporting it read at all: the
                            // read receipt there would start the deletion on this phone too.
                            if (!keepsLocalCopy || isSecretChat(messageArg(param.args))) param.setResult(null);
                        }

                        @Override
                        protected void afterMethod(MethodHookParam param) {
                            if (!ConfigManager.preventMedia.isEnable()) return;
                            Object message = messageArg(param.args);
                            // readNow false: the secret media viewer is about to open it.
                            if (message != null && !readNowArg(param.args) && !isSecretChat(message)) {
                                SecretMediaDeleteButton.showFor(message, param.getResult() != null);
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

                if (keepsLocalCopy && ClassLoad.getClass(ClassNames.MESSAGES_STORAGE) != null) {
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
                                                if (KeptSecretMedia.containsMid(mid) && KeptSecretMedia.contains(MessageObject.getDialogId(message), mid)) {
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
     * Opening a secret media sends "opened" (messages.readMessageContents) through
     * markMessageAsRead2, which also queues the media's deletion on this phone unless told not to.
     * This keeps the request and drops only the deletion, and remembers the media as kept.
     */
    private static boolean hookMarkAsRead() {
        Class<?> controller = ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER);
        if (controller == null) return false;
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
            Logger.w("Prevent media: markMessageAsRead2 not found, secret media is not reported as opened");
            return false;
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
        return true;
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
