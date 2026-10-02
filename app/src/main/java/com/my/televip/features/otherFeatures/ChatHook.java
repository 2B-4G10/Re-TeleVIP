package com.my.televip.features.otherFeatures;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.ActionBar.ActionBarMenuItem;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.Drawables;
import com.my.televip.virtuals.Theme;
import com.my.televip.virtuals.ui.ChatActivity;


public class ChatHook {

    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized || ClientChecker.check(ClientChecker.ClientType.Nagram) || ClientChecker.check(ClientChecker.ClientType.TelegramPlus)) return;

        try {
            initialized = true;
            HMethod.hookMethod(ClassLoad.getClass(ClassNames.CHAT_ACTIVITY), AutomationResolver.resolve("ChatActivity", "createView", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("createView", new Class<?>[]{Context.class}), new AbstractMethodHook() {
                @Override
                protected void afterMethod(MethodHookParam param) {
                    try {
                        ChatActivity chatActivity = new ChatActivity(param.thisObject);

                        ActionBarMenuItem headerItem = chatActivity.getHeaderItem();
                        if (headerItem.getActionBarMenuItem() != null) {

                            int drawableResource = Drawables.id(context, "msg_go_up");

                            if (!ClientChecker.check(ClientChecker.ClientType.iMe) && !ClientChecker.check(ClientChecker.ClientType.iMeWeb) && !ClientChecker.check(ClientChecker.ClientType.TelegramPlus) && !ClientChecker.check(ClientChecker.ClientType.XPlus) && !ClientChecker.check(ClientChecker.ClientType.forkgram) && !ClientChecker.check(ClientChecker.ClientType.forkgramBeta)) {
                                headerItem.lazilyAddSubItem(8353847, drawableResource, Translator.get(Keys.ToTheBeginning));
                            }
                            drawableResource = Drawables.id(context, "player_new_order");

                            headerItem.lazilyAddSubItem(8353848, drawableResource, Translator.get(Keys.ToTheMessage));

                            MenuClicks.attach(param.thisObject, (fragment, id) -> onItemClick(context, fragment, id));
                        }
                    } catch (Throwable t){
                        Logger.e(t);
                    }

                }
            }));

        } catch (Throwable t){
            Logger.e(t);
        }
    }

    private static void onItemClick(Context context, Object fragment, int id) {
        try {
            ChatActivity chat = new ChatActivity(fragment);

            if (id == 8353847) {
                chat.scrollToMessageId(1, 0, true, 0, true, 0);
            } else if (id == 8353848) {

                AlertDialog dialog = new AlertDialog(context);
                dialog.setTitle(Translator.get(Keys.InputMessageId));

                EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                if (Theme.isLight()) {
                    input.setTextColor(0xFF000000);
                    input.setHintTextColor(0xFF424242);
                } else {
                    input.setTextColor(0xFFFFFFFF);
                    input.setHintTextColor(0xFFBDBDBD);
                }
                input.setTextSize(18);
                input.setPadding(20, 20, 20, 20);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20, 20, 20, 20);
                input.setLayoutParams(params);

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(input);

                dialog.setView(layout);

                dialog.setPositiveButton(Translator.get(Keys.Done), AlertDialog.click(() -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        int msgId = Integer.parseInt(text);
                        chat.scrollToMessageId(msgId, 0, true, 0, true, 0);
                    }
                }));

                dialog.show();
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }
}