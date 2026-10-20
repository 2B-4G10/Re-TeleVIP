package com.my.televip.settings.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.my.televip.Configs.ConfigItem;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.audio;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.settings.controller.SettingsController;
import com.my.televip.utils.DialogUtils;
import com.my.televip.utils.Utils;
import com.my.televip.virtuals.Theme;
import com.my.televip.virtuals.messenger.browser.Browser;
import com.my.televip.virtuals.ui.Cells.ExpandableTextCheckCell;
import com.my.televip.virtuals.ui.Cells.TextCheckCell;
import com.my.televip.virtuals.ui.Cells.TextInfoCell;

/** Turns each of ConfigManager's items into a row of TeleVip's settings list. */
public class SettingsAdapter {

    private static boolean isLongText = false;

    public static int getRowCount() {
        return ConfigManager.getItems().size();
    }

    /** One row: its view, and the cell inside it that {@link #bind} fills in. */
    public static final class Row {
        final View itemView;
        SectionHeaderView header;
        TextCheckCell check;
        ActionRowView action;
        ExpandableTextCheckCell expandable;
        TextInfoCell info;

        private Row(View itemView) {
            this.itemView = itemView;
        }
    }

    /**
     * An empty row for the item at {@code position}: the client's own cells for switches, so they
     * look and toggle like the client's, and TeleVip's for headers and buttons. Dividers are not
     * rows; SettingsActivity ends a group's card at one.
     */
    public static Row createRow(Context context, int position) {
        Row row;
        switch (ConfigManager.getItems().get(position).getType()) {
            case ConfigItem.SWITCH: {
                TextCheckCell cell = SettingsCells.createTextCheckCell(context);
                row = new Row(cell.getView());
                row.check = cell;
                break;
            }
            case ConfigItem.TEXT: {
                ActionRowView cell = SettingsCells.createActionRow(context);
                row = new Row(cell);
                row.action = cell;
                break;
            }
            case ConfigItem.INFO: {
                TextInfoCell cell = SettingsCells.createTextInfoCell(context);
                row = new Row(cell);
                row.info = cell;
                break;
            }
            case ConfigItem.EXPANDABLE_SWITCH: {
                ExpandableTextCheckCell cell = SettingsCells.createExpandableTextCheckCell(context);
                row = new Row(cell);
                row.expandable = cell;
                break;
            }
            case ConfigItem.HEADER:
            default: {
                SectionHeaderView cell = SettingsCells.createHeaderCell(context);
                row = new Row(cell);
                row.header = cell;
                break;
            }
        }
        return row;
    }

    /** Shows the item at {@code position} in {@code row} and wires up its clicks. */
    public static void bind(Row row, SettingsController settingsController, int position) {
        try {
            ConfigItem item = ConfigManager.getItems().get(position);
            int viewType = item.getType();

            switch (viewType) {
                case ConfigItem.HEADER:
                    row.header.set(Translator.get(item.getKey()), SettingsIcons.forGroup(item.getKey()));
                    break;

                case ConfigItem.SWITCH:
                    TextCheckCell textCheck = row.check;
                    String explanation = describe(item);
                    boolean divider = hasRowBelow(position);
                    if (explanation != null) {
                        textCheck.setTextAndValueAndCheck(
                                Translator.get(item.getKey()),
                                explanation,
                                item.isEnable(),
                                true,
                                divider
                        );
                    } else {
                        textCheck.setTextAndCheck(
                                Translator.get(item.getKey()),
                                item.isEnable(),
                                divider
                        );
                    }
                    TextView title = textCheck.getTextView();
                    if (title != null) {   // long titles wrap where the title is a TextView
                        title.setLines(0);
                        title.setMaxLines(0);
                        title.setSingleLine(false);
                        title.setEllipsize(null);
                        if (explanation != null) SettingsCells.keepValueBelowTitle(textCheck);
                    }
                    break;
                case ConfigItem.EXPANDABLE_SWITCH:
                    row.expandable.addChildren(item);

                    break;
                case ConfigItem.TEXT:
                    bindAction(row.action, item);
                    row.action.setDivider(hasRowBelow(position));
                    break;
                case ConfigItem.INFO:
                    TextView textView = row.info.getTextView();
                    if (item.getKey().equals(Keys.OfflineVisibilityInfo)) {
                        textView.setMaxLines(2);
                        textView.setEllipsize(TextUtils.TruncateAt.END);
                        textView.setText(Translator.get(Keys.OfflineVisibilityInfo));
                        if (textView.getMaxLines() == 2) {
                            isLongText = false;
                        }
                        textView.setOnClickListener(v -> {
                            if (!isLongText) {
                                textView.setMaxLines(Integer.MAX_VALUE);
                                textView.setEllipsize(null);
                                isLongText = true;
                            } else {
                                textView.setMaxLines(2);
                                textView.setEllipsize(TextUtils.TruncateAt.END);
                                textView.setText(Translator.get(Keys.OfflineVisibilityInfo));
                                isLongText = false;
                            }
                        });
                    }
                    break;
            }
            row.itemView.setOnLongClickListener(v -> {
                playAudio(settingsController.getContext());
                return true;
            });

            row.itemView.setOnClickListener(v -> {

                if (viewType == ConfigItem.SWITCH) {
                    boolean checked = !row.check.isChecked();
                    row.check.setChecked(checked);
                    item.setEnable(checked);
                    item.run();
                } else if (viewType == ConfigItem.TEXT) {
                    switch (item.getKey()) {
                        case Keys.TeleVipGroup:
                            Browser.openUrl(settingsController.getContext(), SettingsController.GROUP_URL);
                            settingsController.hide();
                            break;
                        case Keys.RestartApp:
                            Intent intent = settingsController.getContext()
                                    .getPackageManager()
                                    .getLaunchIntentForPackage(Utils.pkgName);

                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                settingsController.getContext().startActivity(intent);
                            }

                            ((Activity) settingsController.getContext()).finishAffinity();
                            android.os.Process.killProcess(android.os.Process.myPid());
                            break;
                        case Keys.Calendar:
                            Dialog dlg = DialogUtils.createSingleChoiceDialog((Activity) settingsController.getContext(), new String[]{
                                            Translator.get(Keys.Gregorian), Translator.get(Keys.Hijri), Translator.get(Keys.Persian)},
                                    Translator.get(Keys.Calendar), item.getCustomCalendar(), (dialog, which) -> {
                                        item.setCustomCalendar(which);
                                        item.run();
                                        bind(row, settingsController, position);
                                    });
                            dlg.show();
                            break;
                    }
                }
            });

        } catch (Throwable t){
            Logger.e(t);
        }

    }

    /**
     * What the switch does, shown under its title: the item's "Desc" text, else its fixed value,
     * with a note on its own line when it only takes effect after a restart. Null for none.
     */
    public static String describe(ConfigItem item) {
        String text = Translator.optional(item.getKey() + "Desc");
        if (text == null) text = item.getValue();
        if (item.isRestartRequired()) {
            String restart = Translator.get(Keys.RestartRequired);
            text = text == null ? restart : text + "\n" + restart;
        }
        return text;
    }

    /** Whether the item after {@code position} is a row of the same group, so needs a divider. */
    private static boolean hasRowBelow(int position) {
        if (position + 1 >= ConfigManager.getItems().size()) return false;
        int next = ConfigManager.getItems().get(position + 1).getType();
        return next == ConfigItem.SWITCH || next == ConfigItem.EXPANDABLE_SWITCH || next == ConfigItem.TEXT;
    }

    private static void bindAction(ActionRowView view, ConfigItem item) {
        String title = Translator.get(item.getKey());
        String explanation = Translator.optional(item.getKey() + "Desc");
        switch (item.getKey()) {
            case Keys.Calendar: {
                String value = null;
                switch (item.getCustomCalendar()) {
                    case 0:
                        value = Translator.get(Keys.Gregorian);
                        break;
                    case 1:
                        value = Translator.get(Keys.Hijri);
                        break;
                    case 2:
                        value = Translator.get(Keys.Persian);
                        break;
                }
                view.set(title, explanation, value, null);
                break;
            }
            case Keys.TeleVipGroup:
                view.set(title, SettingsController.GROUP_LINK, null, SettingsIcons.logo());
                view.getTitleView().setTextColor(Theme.getTextBlueColor());
                break;
            case Keys.RestartApp:
                view.set(title, explanation, null, SettingsIcons.restart());
                break;
            default:
                view.set(title, explanation, null, null);
                break;
        }
    }

    public static void playAudio(Context context) {
        if (audio.playing) {
            audio.stop();
        } else {
            audio.start();
            DialogUtils.showQuranAlert(context);
        }
    }

}
