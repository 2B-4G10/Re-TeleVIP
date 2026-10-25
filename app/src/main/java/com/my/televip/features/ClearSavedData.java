package com.my.televip.features;

import com.my.televip.Database.MessageDatabase;
import com.my.televip.logging.Logger;

/**
 * Deletes what TeleVip keeps on the phone by itself: the old texts Save edits history kept, and
 * the list of self-destructing media Prevent media kept from expiring. Deleted messages that
 * Show deleted messages kept are part of their chats, and go the way any message does.
 */
public final class ClearSavedData {

    private ClearSavedData() {
    }

    public static void run() {
        try {
            MessageDatabase.deleteAll();
        } catch (Throwable t) {
            Logger.e(t);
        }
        try {
            KeptSecretMedia.clear();
        } catch (Throwable t) {
            Logger.e(t);
        }
    }
}
