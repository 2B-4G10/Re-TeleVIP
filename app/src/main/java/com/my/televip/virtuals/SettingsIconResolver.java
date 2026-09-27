package com.my.televip.virtuals;

import android.content.Context;

public class SettingsIconResolver {

    private static final String[] NAMES = {
            "msg_settings",
            "msg_settings_old",
            "msg_settings_ny",
            "msg_settings_14",
            "msg_settings_hw"
    };

    private static Integer cachedIcon = null;

    /** The client's settings icon, looked up in R$drawable and then in the resource table. */
    public static int getIconSettings(Context context) {
        if (cachedIcon != null) return cachedIcon;
        int id = Drawables.id(context, NAMES);
        if (id != 0) cachedIcon = id;
        return id;
    }

    /** Without a context only R$drawable can be asked; 0 (no icon) when R8 has stripped it. */
    public static int getIconSettings() {
        if (cachedIcon != null) return cachedIcon;
        int id = Drawables.id(null, NAMES);
        if (id != 0) cachedIcon = id;
        return id;
    }

}
