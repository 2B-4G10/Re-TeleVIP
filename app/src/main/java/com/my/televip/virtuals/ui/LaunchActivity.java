package com.my.televip.virtuals.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;

import com.my.televip.reflect.XReflect;

public class LaunchActivity {

    Object launchActivity;
    public FrameLayout frameLayout;

    public LaunchActivity(Object obj){
       launchActivity = obj;
       frameLayout = resolveContent(obj);
    }

    /**
     * The FrameLayout LaunchActivity hands to setContentView. The resolved field is only trusted
     * when it really is that view (a direct child of the window's content), so a mis-resolved
     * field - or none at all - falls back to the content view the window itself reports.
     */
    private static FrameLayout resolveContent(Object obj) {
        FrameLayout field = null;
        try {
            Object value = XReflect.getObjectField(obj, AutomationResolver.resolve("LaunchActivity", "frameLayout",
                    AutomationResolver.ResolverType.Field));
            if (value instanceof FrameLayout) field = (FrameLayout) value;
        } catch (Throwable t) {
            Logger.e(t);
        }
        if (!(obj instanceof Activity)) return field;

        View content = ((Activity) obj).findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) return field;
        ViewGroup root = (ViewGroup) content;
        if (field != null && field.getParent() == root) return field;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof FrameLayout) return (FrameLayout) child;
        }
        return content instanceof FrameLayout ? (FrameLayout) content : field;
    }

}
