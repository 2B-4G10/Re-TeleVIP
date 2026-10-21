package com.my.televip.virtuals.messenger;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.tgnet.TLRPC;

import com.my.televip.reflect.XReflect;

public class MessageObject {

    static Object messageObject;

    public MessageObject(Object obj){
        messageObject = obj;
    }

    public TLRPC.Message getMessageOwner(){
        return new TLRPC.Message(XReflect.getObjectField(messageObject, AutomationResolver.resolve("MessageObject","messageOwner", AutomationResolver.ResolverType.Field)));
    }

    /**
     * The client's own getDialogId() only returns getDialogId(messageOwner), so that is called
     * instead: "MessageObject#getDialogId" maps to the static overload, and on renamed builds
     * (Nekogram, Cherrygram) the instance one has another name, so calling it failed there.
     */
    public long getDialogId() {
        return getDialogId(getMessageOwner());
    }

    public boolean isVoice() {
        return (boolean) XReflect.callMethod(messageObject, AutomationResolver.resolve("MessageObject", "isVoice", AutomationResolver.ResolverType.Method));
    }

    public static long getDialogId(TLRPC.Message message) {
        return (long) XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.MESSAGE_OBJECT), AutomationResolver.resolve("MessageObject", "getDialogId", AutomationResolver.ResolverType.Method), message.message);
    }

    public Object getMessageObject(){
        return messageObject;
    }

}
