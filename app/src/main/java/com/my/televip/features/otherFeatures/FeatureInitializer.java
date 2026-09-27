package com.my.televip.features.otherFeatures;

import android.content.Context;

import com.my.televip.logging.Logger;

public class FeatureInitializer {

    /**
     * Adds TeleVip's entries to the chat and profile menus. Their clicks are routed by
     * {@link MenuClicks} from each fragment's live listener, so nothing has to be learned (and
     * remembered) about listener class names first.
     */
    public static void init(Context context) {
        try {
            ChatHook.init(context);
            ProfileHook.init(context);
        } catch (Throwable t) {
            Logger.e(t);
        }
    }
}
