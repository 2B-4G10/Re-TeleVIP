package com.my.televip.settings.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my.televip.application.AndroidUtilities;
import com.my.televip.language.Translator;
import com.my.televip.virtuals.Theme;

/**
 * A tappable row of the settings: an optional icon tile, a title with an explanation under it, and
 * an optional value at the end (the calendar in use). Built here rather than from the client's
 * TextSettingsCell, which has no second line or icon.
 */
public class ActionRowView extends LinearLayout {

    private final ImageView icon;
    private final LinearLayout texts;
    private final TextView title;
    private final TextView subtitle;
    private final TextView value;
    private final Paint dividerPaint = new Paint();
    private final boolean rtl;
    private boolean divider;

    public ActionRowView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(AndroidUtilities.dp(56));
        setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(10), AndroidUtilities.dp(21), AndroidUtilities.dp(10));

        // Mirrored by hand for a right-to-left language: the settings are laid out left to right.
        rtl = Translator.isRTL();
        int textGravity = rtl ? Gravity.RIGHT : Gravity.LEFT;

        icon = new ImageView(context);
        icon.setVisibility(GONE);
        LayoutParams iconParams = new LayoutParams(AndroidUtilities.dp(36), AndroidUtilities.dp(36));
        if (rtl) {
            iconParams.leftMargin = AndroidUtilities.dp(16);
        } else {
            iconParams.rightMargin = AndroidUtilities.dp(16);
        }

        texts = new LinearLayout(context);
        texts.setOrientation(VERTICAL);
        LayoutParams textsParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);

        title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        title.setTextColor(Theme.getTextColor());
        title.setGravity(textGravity);
        texts.addView(title, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        subtitle = new TextView(context);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        subtitle.setTextColor(Theme.getTextGrayColor());
        subtitle.setPadding(0, AndroidUtilities.dp(2), 0, 0);
        subtitle.setGravity(textGravity);
        subtitle.setVisibility(GONE);
        texts.addView(subtitle, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        value = new TextView(context);
        value.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        value.setTextColor(Theme.getTextBlueColor());
        value.setVisibility(GONE);
        LayoutParams valueParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (rtl) {
            valueParams.rightMargin = AndroidUtilities.dp(12);
            addView(value, valueParams);
            addView(texts, textsParams);
            addView(icon, iconParams);
        } else {
            valueParams.leftMargin = AndroidUtilities.dp(12);
            addView(icon, iconParams);
            addView(texts, textsParams);
            addView(value, valueParams);
        }

        dividerPaint.setColor(Theme.isLight() ? 0x14000000 : 0x14FFFFFF);
        setWillNotDraw(false);
    }

    /** A hairline under the row, from where the text starts, like the client's own rows. */
    public void setDivider(boolean divider) {
        this.divider = divider;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!divider) return;
        int start = rtl ? 0 : texts.getLeft();
        int end = rtl ? texts.getRight() : getWidth();
        canvas.drawRect(start, getHeight() - 1, end, getHeight(), dividerPaint);
    }

    public void set(CharSequence titleText, CharSequence subtitleText, CharSequence valueText, Drawable drawable) {
        title.setText(titleText);
        show(subtitle, subtitleText);
        show(value, valueText);
        icon.setImageDrawable(drawable);
        icon.setVisibility(drawable != null ? VISIBLE : GONE);
    }

    public TextView getTitleView() {
        return title;
    }

    private static void show(TextView view, CharSequence text) {
        view.setText(text);
        view.setVisibility(text == null || text.length() == 0 ? View.GONE : View.VISIBLE);
    }
}
