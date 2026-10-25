package com.my.televip.settings.controller;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.my.televip.Configs.ConfigPreferences;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.settings.ui.SettingsActivity;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.messenger.browser.Browser;
import com.my.televip.virtuals.ui.LaunchActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SettingsController {

    /** The Re: TeleVIP group, opened from the settings' invite row and the join prompt. */
    public static final String GROUP_LINK = "t.me/Revived_TeleVIP";
    public static final String GROUP_URL = "https://" + GROUP_LINK;

    public FrameLayout settingsView;
    private final Context context;

    /**
     * The client's LaunchActivity that is showing now. Leaving the client with Back finishes it
     * and reopening makes a new one in the same process; the settings and their dialogs must go
     * there, since the first one's window is gone (dialogs fail with BadTokenException).
     */
    private static WeakReference<Activity> currentActivity = new WeakReference<>(null);

    public SettingsActivity settingsActivity;

    /**
     * The client's views that were showing when the settings opened, and that the settings hid.
     * Only these are shown again on close: the client keeps views of its own there that are meant
     * to stay hidden, such as the day/night switch's screenshot, which blocks every later theme
     * or colour change while it is showing.
     */
    private final List<View> hiddenViews = new ArrayList<>();

    public SettingsController(Context context) {
        this.context = context;
    }

    public void openView(){
        try {
            Context context = getContext();
            if (settingsView == null || settingsView.getContext() != context) {
                settingsView = new FrameLayout(context);
            }

            settingsView.removeAllViews();
            settingsActivity = new SettingsActivity(context);
            showJoinTeleVip();

            settingsView.addView(settingsActivity.createView(this));

            show(settingsView);
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    /** "Don't show again" for the current group; a new key, so the move to a new group is shown once. */
    private static final String JOIN_DISMISSED = "JoinRevivedTeleVip";

    /** Builds the open settings again, in the language picked just now. */
    public void refresh() {
        try {
            if (settingsView == null) {
                openView();
                return;
            }
            settingsView.removeAllViews();
            settingsActivity = new SettingsActivity(getContext());
            settingsView.addView(settingsActivity.createView(this));
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    private void showJoinTeleVip() {
        try {
            if (!ConfigPreferences.getBoolean(JOIN_DISMISSED)) {
                Context context = getContext();
                AlertDialog alertDialog = new AlertDialog(context);

                alertDialog.setTitle(Translator.get(Keys.SettingsName));
                alertDialog.setMessage(Translator.get(Keys.JoinTeleVip));

                alertDialog.setPositiveButton(Translator.get(Keys.Join), AlertDialog.click(() -> {
                    try {
                        Browser.openUrl(context, GROUP_URL);
                    } catch (Throwable t) {
                        Logger.e(t);
                    }
                    hide();
                }));

                alertDialog.setNegativeButton(Translator.get(Keys.Cancel), null);
                alertDialog.setNeutralButton(Translator.get(Keys.DontShowAgain), AlertDialog.click(() -> ConfigPreferences.putBoolean(JOIN_DISMISSED, true)));
                Translator.withTextDirection(alertDialog::show);
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    public void show(View target) {
        try {
            FrameLayout root = new LaunchActivity(getContext()).frameLayout;
            if (root == null) {
                Logger.e(new IllegalStateException("LaunchActivity content view not found"));
                return;
            }
            if (target.getParent() == null) {
                root.addView(target);
            }

            for (int i = 0; i < root.getChildCount(); i++) {
                View child = root.getChildAt(i);
                if (child == target) {
                    child.setVisibility(View.VISIBLE);
                } else if (child.getVisibility() != View.GONE) {
                    hiddenViews.add(child);
                    child.setVisibility(View.GONE);
                }
            }

            target.bringToFront();
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    public void hide() {
        SettingsActivity.isSettings = false;
        if (settingsView == null) return;
        try {
            // Where the view actually is beats where we think it went.
            ViewGroup parent = settingsView.getParent() instanceof ViewGroup ? (ViewGroup) settingsView.getParent() : null;
            if (parent == null) parent = new LaunchActivity(settingsView.getContext()).frameLayout;
            if (parent == null) return;

            settingsView.setVisibility(View.GONE);
            for (View child : hiddenViews) {
                if (child.getParent() == parent) child.setVisibility(View.VISIBLE);
            }
            hiddenViews.clear();
            if (settingsView.getParent() == parent) {
                parent.removeView(settingsView);
            }
        } catch (Throwable e) {
            Logger.e(e);
        }
    }

    /** Called from every LaunchActivity#onCreate, so the settings follow the client's current one. */
    public static void setActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public Context getContext() {
        Activity activity = currentActivity.get();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) return activity;
        return context;
    }

}
