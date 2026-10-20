package com.my.televip.settings.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.my.televip.virtuals.Theme;
import com.my.televip.virtuals.ui.Cells.ExpandableTextCheckCell;
import com.my.televip.virtuals.ui.Cells.TextCheckCell;
import com.my.televip.virtuals.ui.Cells.TextInfoCell;

/** The cells of TeleVip's settings list, the client's own and TeleVip's, set up as its rows. */
public class SettingsCells {

    /** The client theme's selectableItemBackground, for the rows' touch feedback. */
    private static final TypedValue rowBackground = new TypedValue();

    /** Looks the row background up in {@code context}'s theme; call before creating rows. */
    public static void prepare(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rowBackground, true);
    }

    public static TextCheckCell createTextCheckCell(Context context) {
        TextCheckCell textCheckCell = new TextCheckCell(context);
        textCheckCell.getView().setBackgroundColor(Theme.getBackgroundWhiteOrBlueColor());
        textCheckCell.getView().setBackgroundResource(rowBackground.resourceId);
        textCheckCell.getView().setClickable(true);
        textCheckCell.getView().setFocusable(true);
        return textCheckCell;
    }

    public static ExpandableTextCheckCell createExpandableTextCheckCell(Context context) {
        ExpandableTextCheckCell expandableTextCheckCell = new ExpandableTextCheckCell(context);
        expandableTextCheckCell.setBackgroundColor(Theme.getBackgroundWhiteOrBlueColor());
        expandableTextCheckCell.setBackgroundResource(rowBackground.resourceId);
        expandableTextCheckCell.setBChildResource(rowBackground.resourceId);
        expandableTextCheckCell.setClickable(true);
        expandableTextCheckCell.setFocusable(true);
        return expandableTextCheckCell;
    }

    public static ActionRowView createActionRow(Context context) {
        ActionRowView row = new ActionRowView(context);
        row.setBackgroundResource(rowBackground.resourceId);
        row.setClickable(true);
        row.setFocusable(true);
        return row;
    }

    public static SectionHeaderView createHeaderCell(Context context) {
        return new SectionHeaderView(context);
    }

    public static TextInfoCell createTextInfoCell(Context context) {
        // No background: it sits under its group's card, on the list's.
        return new TextInfoCell(context);
    }

    /**
     * Keeps the explanation under a switch's title when the title wraps. The client's cell puts
     * the explanation at a fixed distance from the top, sized for a one-line title, so a long
     * title (or a long translation) ran over it. Each extra title line moves it down by a line.
     */
    public static void keepValueBelowTitle(TextCheckCell cell) {
        TextView title = cell.getTextView();
        if (title == null || !(title.getParent() instanceof ViewGroup)) return;
        TextView value = nextTextView((ViewGroup) title.getParent(), title);
        if (value == null || !(value.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) return;
        if (value.getTag(TOP_MARGIN_TAG) != null) return;   // already watched (the row was bound again)
        value.setTag(TOP_MARGIN_TAG, ((ViewGroup.MarginLayoutParams) value.getLayoutParams()).topMargin);
        title.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) value.getLayoutParams();
            int extraLines = Math.max(0, title.getLineCount() - 1);
            int want = (Integer) value.getTag(TOP_MARGIN_TAG) + extraLines * title.getLineHeight();
            if (params.topMargin != want) {
                params.topMargin = want;
                // Not from inside this layout pass.
                value.post(() -> value.setLayoutParams(params));
            }
        });
    }

    /** A key of TeleVip's own for View tags, out of the range aapt gives resource ids. */
    private static final int TOP_MARGIN_TAG = 0x7e0e0001;

    private static TextView nextTextView(ViewGroup parent, TextView title) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child != title && child instanceof TextView) return (TextView) child;
        }
        return null;
    }
}
