package com.my.televip.settings.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my.televip.application.AndroidUtilities;
import com.my.televip.virtuals.Theme;

/**
 * The title of a group of settings: the group's icon tile and its name. Built here rather than from
 * the client's HeaderCell, which has no room for an icon.
 */
public class SectionHeaderView extends LinearLayout {

    private final ImageView icon;
    private final TextView title;

    public SectionHeaderView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(14), AndroidUtilities.dp(18), AndroidUtilities.dp(6));

        icon = new ImageView(context);
        icon.setVisibility(GONE);
        LayoutParams iconParams = new LayoutParams(AndroidUtilities.dp(28), AndroidUtilities.dp(28));
        iconParams.setMarginEnd(AndroidUtilities.dp(12));
        addView(icon, iconParams);

        title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(Theme.getTextBlueColor());
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
    }

    public void set(CharSequence text, Drawable drawable) {
        title.setText(text);
        icon.setImageDrawable(drawable);
        icon.setVisibility(drawable != null ? VISIBLE : GONE);
    }
}
