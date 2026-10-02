package com.my.televip.utils;

import android.content.pm.PackageInfo;
import android.os.Build;

import java.util.ArrayList;

public class Utils {
    public static String pkgName = null;
    public static String modulePath = null;
    public static ClassLoader classLoader = null;
    public static final String issue = "Your Telegram client may be an incompatible version with TeleVip. Please download the latest version that is compatible with TeleVip.";

    /** The client's versionCode; PackageInfo.versionCode is deprecated from Android 9. */
    @SuppressWarnings("deprecation")
    public static long versionCode(PackageInfo info) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? info.getLongVersionCode() : info.versionCode;
    }

    public static <T> ArrayList<T> castList(Object obj, Class<T> clazz)
    {
        ArrayList<T> result = new ArrayList<>();
        if (obj instanceof ArrayList<?>)
        {
            for (Object o : (ArrayList<?>) obj)
                result.add(clazz.cast(o));

            return result;
        }
        return result;
    }

    public static String getFieldAsString(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof CharSequence) {
            return value.toString();
        }

        return String.valueOf(value);
    }

}
