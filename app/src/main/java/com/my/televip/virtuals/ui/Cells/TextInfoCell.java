package com.my.televip.virtuals.ui.Cells;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.my.televip.application.AndroidUtilities;
import com.my.televip.language.Translator;
import com.my.televip.virtuals.Theme;
import com.my.televip.virtuals.ui.Components.LayoutHelper;

public class TextInfoCell extends FrameLayout {

    private final TextView textView;

    public TextInfoCell(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(Theme.getTextGrayColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        // A footnote to the group card above it, lined up with the card's text.
        int gravity = Translator.isRTL() ? Gravity.RIGHT : Gravity.LEFT;
        textView.setGravity(gravity);
        textView.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(4));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, gravity, 33, 0, 33, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    public TextView getTextView() {
        return textView;
    }
}
