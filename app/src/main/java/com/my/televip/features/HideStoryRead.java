package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.ClientChecker;
import com.my.televip.virtuals.tgnet.TLConstructor;

public class HideStoryRead {

    public static boolean isReadStoriesRequest(Object object) {
        if (!ClientChecker.isTgnetObfuscated()) {
            String className = object.getClass().getName();
            return className.contains("TL_stories_readStories") ||
                    className.contains("TL_stories_incrementStoryViews");
        } else {
            return TLConstructor.is(object, ClassNames.TL_STORIES_READ_STORIES, TLConstructor.STORIES_READ_STORIES) ||
                    TLConstructor.is(object, ClassNames.TL_STORIES_INCREMENT_STORY_VIEWS, TLConstructor.STORIES_INCREMENT_STORY_VIEWS);
        }
    }
}
