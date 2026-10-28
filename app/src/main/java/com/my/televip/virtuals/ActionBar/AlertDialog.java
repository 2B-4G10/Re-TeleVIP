package com.my.televip.virtuals.ActionBar;

import static com.my.televip.application.AndroidUtilities.dp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.Theme;

/**
 * TeleVip's dialogs, built with the platform's AlertDialog.
 *
 * <p>The client's own builder would look more native, but in an obfuscated build its positive,
 * negative and neutral setters are three identically shaped methods with meaningless names, so
 * an action could land on the wrong button. The platform dialog is never wrong.</p>
 *
 * <p>The platform dialog takes its colours from the activity's theme, which most clients keep
 * light even in night mode, while TeleVip's own views follow the client's light or dark mode.
 * So the dialog is built on the matching light or dark system theme and its background, title,
 * message and buttons are painted from {@link Theme}, the same palette as TeleVip's views.</p>
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
        builder = new android.app.AlertDialog.Builder(context, isLight()
                ? android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Dialog_Alert);
    }

    public void setTitle(CharSequence title) {
        builder.setTitle(tinted(title));
    }

    public void setView(View view) {
        builder.setView(view);
    }

    public void setMessage(CharSequence message) {
        builder.setMessage(tinted(message));
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
        create().show();
    }

    public Dialog create() {
        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> paint(dialog));
        created = dialog;
        return created;
    }

    public Runnable getDismissRunnable() {
        return () -> {
            if (created != null) created.dismiss();
        };
    }

    /** Whether the client is in light mode; light when that can't be read. */
    private static boolean isLight() {
        try {
            return Theme.isLight();
        } catch (Throwable t) {
            Logger.e(t);
            return true;
        }
    }

    private static CharSequence tinted(CharSequence text) {
        if (text == null) return null;
        try {
            SpannableString span = new SpannableString(text);
            span.setSpan(new ForegroundColorSpan(Theme.getTextColor()), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return span;
        } catch (Throwable t) {
            Logger.e(t);
            return text;
        }
    }

    /** Background and buttons in TeleVip's colours, so nothing is left to the activity's theme. */
    private static void paint(android.app.AlertDialog dialog) {
        try {
            Window window = dialog.getWindow();
            if (window != null) {
                GradientDrawable background = new GradientDrawable();
                background.setColor(Theme.getBackgroundWhiteOrBlueColor());
                background.setCornerRadius(dp(14));
                window.setBackgroundDrawable(new InsetDrawable(background, dp(16)));
                // The dialog takes its direction from the client's locale; its buttons and texts
                // follow Re: TeleVIP's language instead, which may read the other way. Not on the
                // window's root view: Android sets that one's direction itself when it lays it out.
                View content = window.findViewById(android.R.id.content);
                if (content != null) {
                    content.setLayoutDirection(Translator.isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
                }
            }
            int accent = Theme.getTextBlueColor();
            for (int which : new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL}) {
                Button button = dialog.getButton(which);
                if (button != null) button.setTextColor(accent);
            }
            mirrorButtons(dialog.getButton(DialogInterface.BUTTON_POSITIVE));
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * Clients that don't declare RTL support (Telegram) lay every view out left to right, whatever
     * direction it is given, so the button row is turned around by hand when it reads the wrong way.
     */
    private static void mirrorButtons(Button positive) {
        if (positive == null || !(positive.getParent() instanceof LinearLayout)) return;
        LinearLayout row = (LinearLayout) positive.getParent();
        boolean rtl = Translator.isRTL();
        if (row.getOrientation() != LinearLayout.HORIZONTAL
                || (row.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) == rtl) return;
        int count = row.getChildCount();
        View[] children = new View[count];
        for (int i = 0; i < count; i++) children[i] = row.getChildAt(i);
        row.removeAllViews();
        for (int i = count - 1; i >= 0; i--) row.addView(children[i]);
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
