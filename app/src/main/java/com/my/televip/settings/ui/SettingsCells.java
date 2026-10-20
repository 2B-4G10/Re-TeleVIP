package com.my.televip.settings.ui;

import android.content.Context;
import android.util.TypedValue;

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
}
