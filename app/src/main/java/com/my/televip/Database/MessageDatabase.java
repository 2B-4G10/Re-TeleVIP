package com.my.televip.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.my.televip.logging.Logger;
import com.my.televip.messages.MessageStorage;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * The old texts Save edits history keeps, one row per earlier version of a message.
 *
 * <p>A message is told apart by the account that received it, its chat and its id: message ids
 * are only unique within a supergroup or channel, or within one account for private chats and
 * small groups. Version 1 keyed rows by the sender and the id alone, so one chat's old text
 * could show as another message's history; its rows cannot be told apart again and are dropped.</p>
 */
public class MessageDatabase extends SQLiteOpenHelper {

    private static final int VERSION = 2;

    private final String TABLE_MESSAGES = "messages";
    private final String COLUMN_ACCOUNT = "account";
    private final String COLUMN_CHAT = "chat";
    private final String COLUMN_MSG_ID = "msg_id";
    private final String COLUMN_MESSAGE = "message";
    private final String COLUMN_MESSAGE_DATE = "message_date";
    private final String COLUMN_MSG_COUNT = "msg_count";

    private static final String WHERE_MESSAGE = "account = ? AND chat = ? AND msg_id = ?";

    private MessageDatabase(Context context) {
        super(context, getDataBasePath(), null, VERSION);
    }

    public static String getDataBasePath() {
        return new File(MessageStorage.getStorageFile(), "saveMessages.db").getAbsolutePath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String TABLE_CREATE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ACCOUNT + " LONG, " +
                COLUMN_CHAT + " LONG, " +
                COLUMN_MSG_ID + " INTEGER, " +
                COLUMN_MSG_COUNT + " INTEGER, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_MESSAGE_DATE + " LONG " +
                ");";
        db.execSQL(TABLE_CREATE);
        db.execSQL("CREATE INDEX messages_key ON " + TABLE_MESSAGES + " (" + COLUMN_ACCOUNT + ", " + COLUMN_CHAT + ", " + COLUMN_MSG_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Another TeleVip build can leave a newer version of this file behind, and Android's
        // default is to refuse to open it. Keep its rows when the table still has every column
        // used here, otherwise start the table over.
        if (!hasColumns(db, COLUMN_ACCOUNT, COLUMN_CHAT, COLUMN_MSG_ID, COLUMN_MSG_COUNT, COLUMN_MESSAGE, COLUMN_MESSAGE_DATE)) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private boolean hasColumns(SQLiteDatabase db, String... columns) {
        Set<String> present = new HashSet<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_MESSAGES + ")", null)) {
            int name = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) present.add(cursor.getString(name));
        }
        for (String column : columns) {
            if (!present.contains(column)) return false;
        }
        return true;
    }

    private static String[] key(long account, long chat, int msgId) {
        return new String[]{String.valueOf(account), String.valueOf(chat), String.valueOf(msgId)};
    }

    public void addMessage(long account, long chat, int msgID, String message) {
        try {
            MessageStorage.getStorage().post(() -> {
                // This runs on the storage thread: an error here must not take the client down.
                try {
                    SQLiteDatabase database = getWritableDatabase();
                    if (!searchMessage(account, chat, msgID, message)) {
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_ACCOUNT, account);
                        values.put(COLUMN_CHAT, chat);
                        values.put(COLUMN_MSG_ID, msgID);
                        values.put(COLUMN_MESSAGE, message);

                        int maxMsgCount = getMaxMessageCount(account, chat, msgID);
                        values.put(COLUMN_MSG_COUNT, maxMsgCount + 1);

                        values.put(COLUMN_MESSAGE_DATE, Calendar.getInstance().getTimeInMillis());

                        database.insert(TABLE_MESSAGES, null, values);
                    }
                } catch (Throwable t) {
                    Logger.e(t);
                }
            });
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    public boolean searchMessage(long account, long chat, int msgId, String message) {
        if (message == null) return false;

        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT 1 FROM " + TABLE_MESSAGES + " WHERE " + WHERE_MESSAGE +
                    " AND " + COLUMN_MESSAGE + " = ? LIMIT 1";
            try (Cursor cursor = database.rawQuery(query, new String[]{
                    String.valueOf(account), String.valueOf(chat), String.valueOf(msgId), message})) {
                return cursor.getCount() > 0;
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
        return false;
    }

    public boolean searchMessage(long account, long chat, int msgId) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT 1 FROM " + TABLE_MESSAGES + " WHERE " + WHERE_MESSAGE + " LIMIT 1";
            try (Cursor cursor = database.rawQuery(query, key(account, chat, msgId))) {
                return cursor.getCount() > 0;
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
        return false;
    }

    public String getMessage(long account, long chat, int msgId) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT " + COLUMN_MESSAGE + " FROM " + TABLE_MESSAGES +
                    " WHERE " + WHERE_MESSAGE + " LIMIT 1";
            try (Cursor cursor = database.rawQuery(query, key(account, chat, msgId))) {
                return cursor.moveToFirst() ? cursor.getString(0) : null;
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
        return null;
    }

    public String getMessage(long account, long chat, int msgID, int msgCount) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT " + COLUMN_MESSAGE + " FROM " + TABLE_MESSAGES +
                    " WHERE " + WHERE_MESSAGE + " AND " + COLUMN_MSG_COUNT + " = ? LIMIT 1";
            try (Cursor cursor = database.rawQuery(query, new String[]{
                    String.valueOf(account), String.valueOf(chat), String.valueOf(msgID), String.valueOf(msgCount)})) {
                return cursor.moveToFirst() ? cursor.getString(0) : null;
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
        return null;
    }

    public long getMessageDate(long account, long chat, int msgID, int msgCount) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT " + COLUMN_MESSAGE_DATE + " FROM " + TABLE_MESSAGES +
                    " WHERE " + WHERE_MESSAGE + " AND " + COLUMN_MSG_COUNT + " = ? LIMIT 1";
            try (Cursor cursor = database.rawQuery(query, new String[]{
                    String.valueOf(account), String.valueOf(chat), String.valueOf(msgID), String.valueOf(msgCount)})) {
                return cursor.moveToFirst() ? cursor.getLong(0) : 0;
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public int getMaxMessageCount(long account, long chat, int msgID) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            String query = "SELECT MAX(" + COLUMN_MSG_COUNT + ") FROM " + TABLE_MESSAGES +
                    " WHERE " + WHERE_MESSAGE;
            try (Cursor cursor = database.rawQuery(query, key(account, chat, msgID))) {
                return cursor.moveToFirst() ? cursor.getInt(0) : 0;
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
        return 0;
    }

    /** Forgets a chat's history, for one account. */
    public void deleteChat(long account, long chat) {
        MessageStorage.getStorage().post(() -> {
            try {
                getWritableDatabase().delete(TABLE_MESSAGES, COLUMN_ACCOUNT + " = ? AND " + COLUMN_CHAT + " = ?",
                        new String[]{String.valueOf(account), String.valueOf(chat)});
            } catch (Throwable t) {
                Logger.e(t);
            }
        });
    }

    /**
     * Deletes every kept text, on the storage thread. Works whether or not Save edits history
     * opened the database in this process.
     */
    public static void deleteAll() {
        MessageStorage.getStorage().post(() -> {
            try {
                MessageDatabase open = instance;
                if (open != null) {
                    SQLiteDatabase db = open.getWritableDatabase();
                    db.delete("messages", null, null);
                    db.execSQL("VACUUM");   // the deleted texts would otherwise stay in free pages
                } else {
                    File file = new File(getDataBasePath());
                    if (file.exists()) SQLiteDatabase.deleteDatabase(file);
                }
            } catch (Throwable t) {
                Logger.e(t);
            }
        });
    }

    private static volatile MessageDatabase instance;

    /** The one database of this process, so a clear and Save edits history see the same file. */
    public static synchronized MessageDatabase get(Context context) {
        if (instance == null) instance = new MessageDatabase(context);
        return instance;
    }
}
