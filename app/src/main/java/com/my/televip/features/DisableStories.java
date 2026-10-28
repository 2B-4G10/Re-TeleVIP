package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.ClientChecker;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class DisableStories {

    public static boolean isEnable = false;

    public static void init() {
        try {
            if (!isEnable) {
                isEnable = true;

                if (ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), "MessagesController", new String[]{"storiesEnabled", "storyEntitiesAllowed",}, new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.disableStories.isEnable()) param.setResult(false);
                        }
                    });

                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.MESSAGES_CONTROLLER), AutomationResolver.resolve("MessagesController", "storyEntitiesAllowed2", AutomationResolver.ResolverType.Method), AutomationResolver.merge(AutomationResolver.resolveObject("storyEntitiesAllowed", new Class<?>[]{ClassLoad.getClass(ClassNames.TLRPC_USER)}), new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.disableStories.isEnable()) param.setResult(false);
                        }
                    }));
                }

                if (ClassLoad.getClass(ClassNames.STORIES_CONTROLLER) != null) {
                    // hasStories(long) marks a dialog as having stories; hasStories() is the
                    // list-wide check, which some builds inline away. Hook whichever exist.
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.STORIES_CONTROLLER), AutomationResolver.resolve("StoriesController", "hasStories2", AutomationResolver.ResolverType.Method), long.class, new AbstractMethodHook() {
                        @Override
                        protected void beforeMethod(MethodHookParam param) {
                            if (ConfigManager.disableStories.isEnable())
                                param.setResult(false);
                        }
                    });
                    keepStoryListsEmpty(ClassLoad.getClass(ClassNames.STORIES_CONTROLLER));
                    if (!ClientChecker.check(ClientChecker.ClientType.NagramX)) {
                        HMethod.hookMethod(ClassLoad.getClass(ClassNames.STORIES_CONTROLLER), AutomationResolver.resolve("StoriesController", "hasStories", AutomationResolver.ResolverType.Method), new AbstractMethodHook() {
                            @Override
                            protected void beforeMethod(MethodHookParam param) {
                                if (ConfigManager.disableStories.isEnable())
                                    param.setResult(false);
                            }
                        });
                    }
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

    /**
     * The chat list shows its row of stories when StoriesController's list of them is not empty.
     * Telegram 13.1 has hasStories() inlined there, so its hook is never asked; instead each of the
     * controller's lists refuses stories while the switch is on, and the row has nothing to show.
     */
    private static void keepStoryListsEmpty(Class<?> controller) {
        final Class<?> peerStories = ClassLoad.getClass(ClassNames.TL_STORIES_PEER_STORIES);
        if (controller == null || peerStories == null) return;
        AbstractMethodHook swapLists = new AbstractMethodHook() {
            @Override
            protected void afterMethod(MethodHookParam param) {
                for (Field field : controller.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.getType() != ArrayList.class) continue;
                    try {
                        field.setAccessible(true);
                        Object list = field.get(param.thisObject);
                        if (list != null && list.getClass() == ArrayList.class) {
                            field.set(param.thisObject, new NoStoriesList(peerStories, (ArrayList<?>) list));
                        }
                    } catch (Throwable t) {
                        Logger.e(t);
                    }
                }
            }
        };
        for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
            HMethod.hookMember(constructor, swapLists);
        }
    }

    /** A list that drops the stories put in it while Disable stories is on, and keeps all else. */
    private static final class NoStoriesList extends ArrayList<Object> {
        private final Class<?> peerStories;

        NoStoriesList(Class<?> peerStories, Collection<?> items) {
            super(items);
            this.peerStories = peerStories;
        }

        private boolean drops(Object item) {
            return peerStories.isInstance(item) && ConfigManager.disableStories.isEnable();
        }

        @Override
        public boolean add(Object item) {
            return !drops(item) && super.add(item);
        }

        @Override
        public void add(int index, Object item) {
            if (!drops(item)) super.add(index, item);
        }

        @Override
        public boolean addAll(Collection<?> items) {
            boolean changed = false;
            for (Object item : items) changed |= add(item);
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<?> items) {
            int at = index;
            for (Object item : items) {
                if (drops(item)) continue;
                super.add(at++, item);
            }
            return at != index;
        }
    }
}
