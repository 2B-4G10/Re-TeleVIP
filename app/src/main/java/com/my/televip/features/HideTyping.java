package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.virtuals.tgnet.TLConstructor;

public class HideTyping {

    public static boolean isTypingRequest(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
            String className = object.getClass().getName();
            return className.contains("TL_messages_setTyping") ||
                    className.contains("TL_messages_setEncryptedTyping");
        } else {
            return TLConstructor.is(object, ClassNames.TL_MESSAGES_SET_TYPING, TLConstructor.MESSAGES_SET_TYPING) ||
                    TLConstructor.is(object, ClassNames.TL_MESSAGES_SET_ENCRYPTED_TYPING, TLConstructor.MESSAGES_SET_ENCRYPTED_TYPING);
        }
    }
}
