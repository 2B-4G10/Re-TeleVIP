package com.my.televip.messages;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.my.televip.application.ApplicationLoaderHook;
import com.my.televip.features.ShowDeletedMessages;
import com.my.televip.logging.Logger;
import com.my.televip.virtuals.SQLite.SQLiteCursor;
import com.my.televip.virtuals.SQLite.SQLiteDatabase;
import com.my.televip.virtuals.SQLite.SQLitePreparedStatement;
import com.my.televip.virtuals.messenger.MessagesStorage;
import com.my.televip.virtuals.tgnet.NativeByteBuffer;

import java.io.File;
import java.util.ArrayList;

public class MessageStorage {

    private static final Handler storage = new Handler(makeLooper("Storage"));

    public static File getStorageFile() {
        File dir = new File(
                ApplicationLoaderHook.getApplicationContext()
                        .getFilesDir().getParentFile(),
                "TeleVip"
        );
        if (!dir.exists() && !dir.mkdir()) {
            Logger.w("Cannot create " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static Looper makeLooper(String str) {
        HandlerThread handlerThread = new HandlerThread("TeleVip - " + str, Process.THREAD_PRIORITY_DISPLAY);
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public static void markMessagesDeleted(MessagesStorage messagesStorage, final long dialogId, ArrayList<Integer> delMsg) {
        try {
            // On the client's own storage queue, in turn with its database work: run from a thread
            // of our own, this raced it, and left a statement open each time.
            messagesStorage.getStorageQueue().postRunnable(() -> {
                SQLiteDatabase db = messagesStorage.getDatabase();
                for (int i = 0; i < 2; ++i) {
                    final String table = i == 0 ? "messages_v2" : "messages_topics";

                    String query = "SELECT data,mid,uid " +
                            "FROM "+ table + " "+
                            "WHERE " + (dialogId == 0 ? "is_channel" : "uid") + " = " + dialogId + " AND mid IN (" + TextUtils.join(",", delMsg) + ");";

                    String update = "UPDATE " + table +" SET data = ? WHERE uid = ? AND mid = ?";
                    SQLiteCursor cursor = null;
                    SQLitePreparedStatement state = null;
                    try {
                        cursor = db.queryFinalized(query, new Object[]{});
                        state = db.executeFast(update);

                        while (cursor.next()) {
                            NativeByteBuffer data = cursor.byteBufferValue(0);
                            int mid = cursor.intValue(1);
                            long lastDialogId = cursor.longValue(2);

                            data.position(4);
                            int flags = data.readInt32(true);
                            flags |= ShowDeletedMessages.FLAG_DELETED;
                            data.position(4);
                            data.writeInt32(flags);
                            data.position(0);

                            state.requery();
                            state.bindByteBuffer(1, data);
                            state.bindLong(2, lastDialogId);
                            state.bindInteger(3, mid);
                            state.step();

                            data.reuse();
                        }
                    } catch (Throwable e) {
                        Logger.e(e);
                    } finally {
                        if (cursor != null) cursor.dispose();
                        if (state != null) state.dispose();
                    }
                }
            });
        } catch (Throwable e){
            Logger.e(e);
        }
    }

    public static Handler getStorage(){
        return storage;
    }

}