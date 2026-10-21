package com.my.televip.features;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.my.televip.Class.ClassLoad;
import com.my.televip.Class.ClassNames;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.reflect.XReflect;
import com.my.televip.virtuals.ActionBar.AlertDialog;
import com.my.televip.virtuals.messenger.FileLoader;
import com.my.televip.virtuals.messenger.MessageObject;
import com.my.televip.virtuals.messenger.MessagesController;
import com.my.televip.virtuals.messenger.UserConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static com.my.televip.application.AndroidUtilities.dp;

/**
 * Save, Mark as seen and Delete for both in the secret media viewer, while Prevent media keeps
 * opened media. Opening it tells the sender nothing; "Mark as seen" does, and "Delete for both"
 * removes it here and for the sender, as the timer or the single view would have.
 *
 * <p>Found without names, so it needs nothing renamed builds have to map: the viewer is the
 * static instance of its class, and the buttons sit next to its action bar.</p>
 */
public final class SecretMediaButtons {

    private static final String TAG = "TeleVipSecretMediaButtons";

    private SecretMediaButtons() {
    }

    /**
     * The viewer is opening {@code messageObject}; {@code ttl} is its timer, read before the
     * viewer's hook cleared it. Runs once the viewer is showing it.
     */
    public static void showFor(Object messageObject, int ttl) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                attach(messageObject, ttl);
            } catch (Throwable t) {
                Logger.e(t);
            }
        });
    }

    private static void attach(Object messageObject, int ttl) {
        Class<?> viewerClass = ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER);
        Class<?> actionBarClass = ClassLoad.getClass(ClassNames.ACTION_BAR);
        if (viewerClass == null || actionBarClass == null) return;
        Object viewer = staticInstance(viewerClass);
        View actionBar = viewer == null ? null : (View) fieldOfType(viewer, actionBarClass);
        if (actionBar == null || !(actionBar.getParent() instanceof FrameLayout)) return;
        FrameLayout container = (FrameLayout) actionBar.getParent();

        LinearLayout row = container.findViewWithTag(TAG);
        if (row == null) {
            row = create(container, actionBar);
        }
        TextView save = (TextView) row.getChildAt(0);
        TextView seen = (TextView) row.getChildAt(1);
        TextView delete = (TextView) row.getChildAt(2);

        MessageObject message = new MessageObject(messageObject);
        long dialogId = message.getDialogId();
        int mid = message.getMessageOwner().getID();
        // Secret chats are left as they were: their files are encrypted, and their read receipt
        // would start the deletion on this phone too.
        boolean show = ConfigManager.preventMedia.isEnable() && mid > 0;
        row.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;

        boolean seenAlready = KeptSecretMedia.contains(dialogId, mid);
        seen.setVisibility(PreventMedia.canMarkSeen() && !seenAlready ? View.VISIBLE : View.GONE);
        save.setOnClickListener(v -> save(v.getContext(), messageObject));
        seen.setOnClickListener(v -> {
            if (PreventMedia.markSeen(dialogId, mid, ttl)) {
                v.setVisibility(View.GONE);
                toast(v.getContext(), Translator.get(Keys.SecretMediaMarkedSeen));
            }
        });
        delete.setOnClickListener(v -> confirm(v, viewer, dialogId, mid));
    }

    private static LinearLayout create(FrameLayout container, View actionBar) {
        Context context = container.getContext();
        LinearLayout row = new LinearLayout(context);
        row.setTag(TAG);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(pill(context, Translator.get(Keys.Save)));
        row.addView(pill(context, Translator.get(Keys.SecretMediaMarkSeen)));
        row.addView(pill(context, Translator.get(Keys.SecretMediaDeleteForBoth)));

        // Above the video seek bar, and shown and hidden with the action bar on a tap.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.bottomMargin = dp(64);
        container.addView(row, params);
        final LinearLayout view = row;
        row.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                float alpha = actionBar.getVisibility() == View.VISIBLE ? actionBar.getAlpha() : 0f;
                if (view.getAlpha() != alpha) view.setAlpha(alpha);
                boolean enabled = alpha > 0.5f;
                for (int i = 0; i < view.getChildCount(); i++) view.getChildAt(i).setEnabled(enabled);
                return true;
            }
        });
        return row;
    }

    private static TextView pill(Context context, String text) {
        TextView button = new TextView(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setPadding(dp(14), dp(9), dp(14), dp(9));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xcc000000);
        background.setStroke(dp(1), 0x40ffffff);
        background.setCornerRadius(dp(20));
        button.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dp(4);
        params.rightMargin = dp(4);
        button.setLayoutParams(params);
        return button;
    }

    private static void save(Context context, Object messageObject) {
        File file;
        try {
            file = FileLoader.getInstance(UserConfig.getSelectedAccount())
                    .getPathToMessage(new MessageObject(messageObject).getMessageOwner().message);
        } catch (Throwable t) {
            Logger.e(t);
            file = null;
        }
        if (file == null || !file.isFile() || file.length() == 0) {
            toast(context, Translator.get(Keys.SecretMediaNotDownloaded));
            return;
        }
        File source = file;
        new Thread(() -> {
            boolean saved = copyToGallery(context, source);
            new Handler(Looper.getMainLooper()).post(() ->
                    toast(context, Translator.get(saved ? Keys.SecretMediaSaved : Keys.SecretMediaSaveFailed)));
        }, "TeleVipSaveSecretMedia").start();
    }

    /** Pictures/Telegram or Movies/Telegram, where the client saves media too. */
    private static boolean copyToGallery(Context context, File source) {
        String name = source.getName();
        String lower = name.toLowerCase();
        boolean video = lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".webm");
        String mime = video ? (lower.endsWith(".webm") ? "video/webm" : "video/mp4")
                : (lower.endsWith(".png") ? "image/png" : "image/jpeg");
        if (!name.contains(".")) name += video ? ".mp4" : ".jpg";
        String folder = video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, folder + "/Telegram");
                Uri collection = video
                        ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        : MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = resolver.insert(collection, values);
                if (uri == null) return false;
                try (InputStream in = new FileInputStream(source); OutputStream out = resolver.openOutputStream(uri)) {
                    if (out == null) return false;
                    copy(in, out);
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(folder), "Telegram");
                if (!dir.isDirectory() && !dir.mkdirs()) return false;
                File target = new File(dir, name);
                try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(target)) {
                    copy(in, out);
                }
                MediaScannerConnection.scanFile(context, new String[]{target.getAbsolutePath()}, new String[]{mime}, null);
            }
            return true;
        } catch (Throwable t) {
            Logger.e(t);
            return false;
        }
    }

    private static void copy(InputStream in, OutputStream out) throws java.io.IOException {
        byte[] buffer = new byte[64 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
    }

    private static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private static void confirm(View button, Object viewer, long dialogId, int mid) {
        AlertDialog dialog = new AlertDialog(button.getContext());
        dialog.setTitle(Translator.get(Keys.SecretMediaDeleteTitle));
        dialog.setMessage(Translator.get(Keys.SecretMediaDeleteMessage));
        dialog.setPositiveButton(Translator.get(Keys.Delete), AlertDialog.click(() -> delete(button, viewer, dialogId, mid)));
        dialog.setNegativeButton(Translator.get(Keys.Cancel), null);
        dialog.show();
    }

    private static void delete(View button, Object viewer, long dialogId, int mid) {
        ((View) button.getParent()).setVisibility(View.GONE);
        try {
            XReflect.callMethod(viewer, AutomationResolver.resolve("SecretMediaViewer", "closePhoto", AutomationResolver.ResolverType.Method), true, false);
        } catch (Throwable t) {
            Logger.e(t);
        }
        KeptSecretMedia.remove(dialogId, mid);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(mid);
        if (!MessagesController.getInstance(UserConfig.getSelectedAccount()).deleteMessages(ids, dialogId, true)) {
            Logger.w("Delete for both: deleteMessages not found");
        }
    }

    private static Object staticInstance(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == type) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value != null) return value;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /** R8 may narrow the field to the anonymous ActionBar subclass the viewer creates. */
    private static Object fieldOfType(Object owner, Class<?> type) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value != null) return value;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }
}
