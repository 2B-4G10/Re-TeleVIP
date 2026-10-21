package com.my.televip.features;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.messenger.MessageObject;
import com.my.televip.virtuals.messenger.MessagesController;
import com.my.televip.virtuals.messenger.UserConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static com.my.televip.application.AndroidUtilities.dp;

/**
 * "Delete for both" in the secret media viewer, while Prevent media keeps opened media: removes
 * the kept copy here and the sender's, as the timer or the single view would have.
 *
 * <p>Found without names, so it needs nothing renamed builds have to map: the viewer is the
 * static instance of its class, and the button sits next to its action bar.</p>
 */
final class SecretMediaDeleteButton {

    private static final String TAG = "TeleVipSecretMediaDelete";

    private SecretMediaDeleteButton() {
    }

    /**
     * ChatActivity is about to open {@code messageObject} in the viewer; {@code firstOpen} when it
     * also reported it opened just now. Runs once the viewer is showing it.
     */
    static void showFor(Object messageObject, boolean firstOpen) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                attach(messageObject, firstOpen);
            } catch (Throwable t) {
                Logger.e(t);
            }
        });
    }

    private static void attach(Object messageObject, boolean firstOpen) {
        Class<?> viewerClass = ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER);
        Class<?> actionBarClass = ClassLoad.getClass(ClassNames.ACTION_BAR);
        if (viewerClass == null || actionBarClass == null) return;
        Object viewer = staticInstance(viewerClass);
        View actionBar = viewer == null ? null : (View) fieldOfType(viewer, actionBarClass);
        if (actionBar == null || !(actionBar.getParent() instanceof FrameLayout)) return;
        FrameLayout container = (FrameLayout) actionBar.getParent();

        TextView button = container.findViewWithTag(TAG);
        if (button == null) {
            button = create(container, actionBar);
        }

        MessageObject message = new MessageObject(messageObject);
        long dialogId = message.getDialogId();
        int mid = message.getMessageOwner().getID();
        boolean show = ConfigManager.preventMedia.isEnable() && mid > 0
                && (firstOpen || KeptSecretMedia.contains(dialogId, mid));
        button.setVisibility(show ? View.VISIBLE : View.GONE);
        button.setOnClickListener(v -> confirm(v, viewer, dialogId, mid));
    }

    private static TextView create(FrameLayout container, View actionBar) {
        TextView button = new TextView(container.getContext());
        button.setTag(TAG);
        button.setText(Translator.get(Keys.SecretMediaDeleteForBoth));
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(18), dp(10), dp(18), dp(10));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xcc000000);
        background.setStroke(dp(1), 0x40ffffff);
        background.setCornerRadius(dp(22));
        button.setBackground(background);

        // Above the video seek bar, and shown and hidden with the action bar on a tap.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.bottomMargin = dp(64);
        container.addView(button, params);
        final TextView view = button;
        button.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                float alpha = actionBar.getVisibility() == View.VISIBLE ? actionBar.getAlpha() : 0f;
                if (view.getAlpha() != alpha) view.setAlpha(alpha);
                view.setEnabled(alpha > 0.5f);
                return true;
            }
        });
        return button;
    }

    private static void confirm(View button, Object viewer, long dialogId, int mid) {
        AlertDialog dialog = new AlertDialog(button.getContext());
        dialog.setTitle(Translator.get(Keys.SecretMediaDeleteTitle));
        dialog.setMessage(Translator.get(Keys.SecretMediaDeleteMessage));
        dialog.setPositiveButton(Translator.get(Keys.Delete), AlertDialog.click(() -> delete(button, viewer, dialogId, mid)));
        dialog.setNegativeButton(Translator.get(Keys.Cancel), null);
        dialog.show();
    }

    private static void delete(View button, Object viewer, long dialogId, int mid) {
        button.setVisibility(View.GONE);
        try {
            XReflect.callMethod(viewer, AutomationResolver.resolve("SecretMediaViewer", "closePhoto", AutomationResolver.ResolverType.Method), true, false);
        } catch (Throwable t) {
            Logger.e(t);
        }
        KeptSecretMedia.remove(dialogId, mid);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(mid);
        if (!MessagesController.getInstance(UserConfig.getSelectedAccount()).deleteMessages(ids, dialogId, true)) {
            Logger.w("Delete for both: deleteMessages not found");
        }
    }

    private static Object staticInstance(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == type) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value != null) return value;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /** R8 may narrow the field to the anonymous ActionBar subclass the viewer creates. */
    private static Object fieldOfType(Object owner, Class<?> type) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value != null) return value;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }
}
