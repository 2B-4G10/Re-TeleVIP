package com.my.televip.features.otherFeatures;

import android.content.Context;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.utils.IdDateEstimator;
import com.my.televip.virtuals.ActionBar.ActionBarMenuItem;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.Drawables;
import com.my.televip.virtuals.ui.ProfileActivity;

import com.my.televip.reflect.XReflect;

public class ProfileHook {

    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) return;
        initialized = true;

        HMethod.hookMethod(ClassLoad.getClass(ClassNames.PROFILE_ACTIVITY), AutomationResolver.resolve("ProfileActivity", "createActionBarMenu", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("createActionBarMenu", new Class[]{boolean.class}), new AbstractMethodHook() {
            @Override
            protected void afterMethod(MethodHookParam param) {
                ProfileActivity profileActivity = new ProfileActivity(param.thisObject);
                if (getUserID(profileActivity) > 1) {

                    ActionBarMenuItem otherItem = profileActivity.getOtherItem();

                    if (otherItem.getActionBarMenuItem() != null) {

                        int drawableResource = Drawables.id(context, "msg_filled_menu_users");
                        if (drawableResource == 0 && (ClientChecker.check(ClientChecker.ClientType.Nagram) || ClientChecker.check(ClientChecker.ClientType.Momogram))) {
                            drawableResource = 0x7f0806d3;
                        }

                        otherItem.addSubItem(8353847, drawableResource, Translator.get(Keys.ApproximateCreationDate));
                        MenuClicks.attach(param.thisObject, (fragment, id) -> onItemClick(context, fragment, id));
                    }
                }
            }
        }));
    }

    private static void onItemClick(Context context, Object fragment, int id) {
        if (id != 8353847) return;
        ProfileActivity profile = new ProfileActivity(fragment);

        AlertDialog alertDialog = new AlertDialog(context);
        alertDialog.setTitle(Translator.get(Keys.TeleVip));
        alertDialog.setMessage("\n" +
                Translator.get(Keys.ApproximateCreationDate) + " : " +
                        IdDateEstimator.getYearAndMethod(getUserID(profile)) + "\n\n" +
                        Translator.get(Keys.Age) + " : " +
                        IdDateEstimator.getAge(getUserID(profile)) + "\n\n" +
                        Translator.get(Keys.ApproximateCreationDateNotice)
        );
        alertDialog.setPositiveButton(Translator.get(Keys.Done), null);
        alertDialog.show();
    }

    private static long getUserID(ProfileActivity profile) {
        if (profile.getUserId() > 1) {
            return profile.getUserId();
        }
        return 0;
    }

}