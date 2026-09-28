package com.my.televip.virtuals.ui;

import android.view.View;

import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.Sig;
import com.my.televip.virtuals.ActionBar.ActionBarMenuItem;
import com.my.televip.virtuals.messenger.MessageObject;

import com.my.televip.reflect.XReflect;

public class ChatActivity {


    final Object chatActivity;

    public ChatActivity(Object obj){
        chatActivity = obj;
    }

    public MessageObject getSelectedObject(){
        return new MessageObject(XReflect.getObjectField(chatActivity, AutomationResolver.resolve("ChatActivity","selectedObject", AutomationResolver.ResolverType.Field)));
    }

    public ActionBarMenuItem getHeaderItem(){
        return new ActionBarMenuItem(XReflect.getObjectField(chatActivity, AutomationResolver.resolve("ChatActivity", "headerItem", AutomationResolver.ResolverType.Field)));
    }
    public View getPinnedMessageView(){
        return (View) XReflect.getObjectField(chatActivity, AutomationResolver.resolve("ChatActivity", "pinnedMessageView", AutomationResolver.ResolverType.Field));
    }

    public void scrollToMessageId(int id, int fromMessageId, boolean select, int loadIndex, boolean forceScroll, int forcePinnedMessageId){
        Class<?>[] six = {int.class, int.class, boolean.class, int.class, boolean.class, int.class};
        try {
            Sig.call(chatActivity, AutomationResolver.resolve("ChatActivity", "scrollToMessageId", AutomationResolver.ResolverType.Method),
                    void.class, six, id, fromMessageId, select, loadIndex, forceScroll, forcePinnedMessageId);
            return;
        } catch (Throwable inlined) {
            // R8 inlines the six-argument overload where nothing else calls it (Telegram 12.10.5);
            // every overload forwards to this one, with nulls for the rest.
        }
        Class<?>[] all = {int.class, int.class, boolean.class, int.class, boolean.class, int.class,
                Integer.class, byte[].class, Runnable.class};
        try {
            Sig.call(chatActivity, AutomationResolver.resolveOverload("ChatActivity", "scrollToMessageIdIIZIZIIABR", "scrollToMessageId"),
                    void.class, all, id, fromMessageId, select, loadIndex, forceScroll, forcePinnedMessageId, null, null, null);
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

}
