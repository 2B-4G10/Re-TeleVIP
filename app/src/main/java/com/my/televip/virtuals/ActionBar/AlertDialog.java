package com.my.televip.virtuals.ActionBar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.my.televip.logging.Logger;

/**
 * TeleVip's dialogs, built with the platform's AlertDialog.
 *
 * <p>The client's own builder would look more native, but in an obfuscated build its positive,
 * negative and neutral setters are three identically shaped methods with meaningless names, so
 * an action could land on the wrong button. The platform dialog is never wrong.</p>
 */
public class AlertDialog {

    @FunctionalInterface
    public interface OnClick {
        void onClick();
    }

    /** A button action. Opaque to callers. */
    public static final class Click {
        final OnClick action;

        Click(OnClick action) {
            this.action = action;
        }
    }

    public static Object click(OnClick lambda) {
        return new Click(lambda);
    }

    private final android.app.AlertDialog.Builder builder;
    private Dialog created;

    public AlertDialog(Context context) {
        builder = new android.app.AlertDialog.Builder(context);
    }

    public void setTitle(CharSequence title) {
        builder.setTitle(title);
    }

    public void setView(View view) {
        builder.setView(view);
    }

    public void setMessage(CharSequence message) {
        builder.setMessage(message);
    }

    public void setPositiveButton(CharSequence text, Object click) {
        builder.setPositiveButton(text, listener(click));
    }

    public void setNegativeButton(CharSequence text, Object click) {
        builder.setNegativeButton(text, listener(click));
    }

    public void setNeutralButton(CharSequence text, Object click) {
        builder.setNeutralButton(text, listener(click));
    }

    public void show() {
        created = builder.show();
    }

    public Dialog create() {
        created = builder.create();
        return created;
    }

    public Runnable getDismissRunnable() {
        return () -> {
            if (created != null) created.dismiss();
        };
    }

    private static DialogInterface.OnClickListener listener(Object click) {
        if (!(click instanceof Click)) return null;
        final OnClick action = ((Click) click).action;
        return (dialog, which) -> run(action);
    }

    /** A button action must never take the client down with it. */
    private static void run(OnClick action) {
        try {
            action.onClick();
        } catch (Throwable t) {
            Logger.e(t);
        }
    }
}
