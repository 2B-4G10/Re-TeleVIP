package com.my.televip.virtuals.ActionBar;

import android.text.Layout;
import android.view.View;

import com.my.televip.obfuscate.AutomationResolver;

import com.my.televip.logging.Logger;
import com.my.televip.reflect.Sig;
import com.my.televip.reflect.XReflect;

public class SimpleTextView {

    Object simpleTextView;

    public SimpleTextView(Object textview){
        simpleTextView = textview;
    }

    // setText and getText have shapes no other SimpleTextView method shares, so they are found
    // by shape when the name is not mapped. setAlignment and setMaxLines are cosmetic: skipped
    // if R8 inlined or renamed them beyond recognition.

    public CharSequence getText(){
        try {
            return (CharSequence) Sig.call(simpleTextView, name("getText"), CharSequence.class, new Class[0]);
        } catch (Throwable t) {
            Logger.e(t);
            return null;
        }
    }

    public void setText(CharSequence text){
        try {
            Sig.call(simpleTextView, name("setText"), boolean.class, new Class[]{CharSequence.class}, text);
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    public void setText(CharSequence text, boolean force){
        try {
            Sig.call(simpleTextView, name("setText"), boolean.class, new Class[]{CharSequence.class, boolean.class}, text, force);
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    public void setAlignment(Layout.Alignment alignment){
        try {
            XReflect.callMethod(simpleTextView, name("setAlignment"), alignment);
        } catch (Throwable ignored) {
        }
    }

    public void setMaxLines(int value){
        try {
            XReflect.callMethod(simpleTextView, name("setMaxLines"), value);
        } catch (Throwable ignored) {
        }
    }

    private static String name(String method) {
        return AutomationResolver.resolve("SimpleTextView", method, AutomationResolver.ResolverType.Method);
    }

    public View getSimpleTextView(){
        return (View) simpleTextView;
    }

}
