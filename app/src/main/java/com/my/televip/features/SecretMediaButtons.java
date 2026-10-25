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
import com.my.televip.virtuals.tgnet.TLRPC;

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
 * removes it here and for the sender, as the timer or the single view would have. With Prevent
 * media off the media expires as sent, and only Save is added.
 *
 * <p>Found without names, so it needs nothing renamed builds have to map: the viewer is the
 * static instance of its class, and the buttons sit next to its action bar.</p>
 */
public final class SecretMediaButtons {

    private static final String TAG = "TeleVipSecretMediaButtons";

    private SecretMediaButtons() {
    }

    /** How long to wait for the viewer's window to show, checked every RETRY_MS. */
    private static final int RETRIES = 15;
    private static final int RETRY_MS = 100;

    /**
     * A viewer is opening {@code messageObject}; {@code ttl} is its timer, read before anything
     * cleared it. Attaches once the viewer is on screen.
     */
    public static void showFor(Object messageObject, int ttl) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            int tries;

            @Override
            public void run() {
                try {
                    if (attach(messageObject, ttl)) return;
                    if (++tries < RETRIES) {
                        handler.postDelayed(this, RETRY_MS);
                    } else {
                        Logger.w("Secret media buttons: no viewer on screen");
                    }
                } catch (Throwable t) {
                    Logger.e(t);
                }
            }
        });
    }

    private static boolean attach(Object messageObject, int ttl) {
        Object[] target = findViewer();
        if (target == null) return false;
        Object viewer = target[0];
        View actionBar = (View) target[1];
        boolean photoViewer = target[2] != null;
        FrameLayout container = (FrameLayout) actionBar.getParent();

        LinearLayout row = container.findViewWithTag(TAG);
        if (row == null) {
            row = create(container, actionBar, photoViewer);
        }
        TextView save = (TextView) row.getChildAt(0);
        TextView seen = (TextView) row.getChildAt(1);
        TextView delete = (TextView) row.getChildAt(2);

        TLRPC.Message owner = new MessageObject(messageObject).getMessageOwner();
        int mid = owner.getID();
        row.setVisibility(mid != 0 ? View.VISIBLE : View.GONE);
        if (mid == 0) return true;
        save.setOnClickListener(v -> save(v.getContext(), owner.message));
        if (!ConfigManager.preventMedia.isEnable()) {
            // It expires as sent; Save is the only thing added.
            seen.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            return true;
        }
        if (mid < 0) {
            // A secret chat: its read receipt starts the timer on this phone too, so "Mark as seen"
            // and "Delete for both" are one thing there, the timer running out on both phones.
            seen.setVisibility(View.GONE);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(v -> confirm(v, () -> {
                ((View) v.getParent()).setVisibility(View.GONE);
                close(viewer, photoViewer);
                if (!PreventMedia.readSecretChatMedia(messageObject)) Logger.w("Delete for both: no secret read to send");
            }));
            return true;
        }
        long dialogId = PreventMedia.dialogIdOf(messageObject);

        boolean seenAlready = KeptSecretMedia.contains(dialogId, mid);
        seen.setVisibility(PreventMedia.canMarkSeen() && !seenAlready ? View.VISIBLE : View.GONE);
        seen.setOnClickListener(v -> {
            if (PreventMedia.markSeen(dialogId, mid, ttl)) {
                v.setVisibility(View.GONE);
                toast(v.getContext(), Translator.get(Keys.SecretMediaMarkedSeen));
            }
        });
        delete.setVisibility(View.VISIBLE);
        delete.setOnClickListener(v -> confirm(v, () -> delete(v, viewer, photoViewer, dialogId, mid)));
        return true;
    }

    /**
     * {viewer, its action bar, non-null for PhotoViewer} for the viewer on screen: PhotoViewer in
     * current builds ("Disappearing Photo"), SecretMediaViewer in older ones. Each is the static
     * instance of its class, so nothing has to be mapped by name.
     */
    private static Object[] findViewer() {
        Class<?> actionBarClass = ClassLoad.getClass(ClassNames.ACTION_BAR);
        if (actionBarClass == null) return null;
        Class<?>[] viewers = {ClassLoad.getClass(ClassNames.SECRET_MEDIA_VIEWER), ClassLoad.getClass(ClassNames.PHOTO_VIEWER)};
        for (int i = 0; i < viewers.length; i++) {
            if (viewers[i] == null) continue;
            for (Object viewer : staticInstances(viewers[i])) {
                View actionBar = shownActionBar(viewer, actionBarClass);
                if (actionBar != null) return new Object[]{viewer, actionBar, i == 1 ? Boolean.TRUE : null};
            }
        }
        return null;
    }

    private static LinearLayout create(FrameLayout container, View actionBar, boolean photoViewer) {
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
        // PhotoViewer has its own bar along the bottom.
        params.bottomMargin = dp(photoViewer ? 112 : 64);
        follow(row, actionBar);
        container.addView(row, params);
        return row;
    }

    /**
     * Fades the row with the action bar. Registered again each time the viewer's window is shown:
     * a closed viewer drops its window, and the listeners on it with it, so the row stayed
     * invisible from the second secret photo on.
     */
    private static void follow(LinearLayout row, View actionBar) {
        ViewTreeObserver.OnPreDrawListener fade = () -> {
            float alpha = actionBar.getVisibility() == View.VISIBLE ? actionBar.getAlpha() : 0f;
            if (row.getAlpha() != alpha) row.setAlpha(alpha);
            boolean enabled = alpha > 0.5f;
            for (int i = 0; i < row.getChildCount(); i++) row.getChildAt(i).setEnabled(enabled);
            return true;
        };
        row.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                v.getViewTreeObserver().addOnPreDrawListener(fade);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                v.getViewTreeObserver().removeOnPreDrawListener(fade);
                // PhotoViewer shows every other photo too: the row is only for the secret one.
                v.setVisibility(View.GONE);
            }
        });
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

    private static void save(Context context, Object message) {
        File file;
        try {
            file = FileLoader.getInstance(UserConfig.getSelectedAccount()).getPathToMessage(message);
        } catch (Throwable t) {
            Logger.e(t);
            file = null;
        }
        // Timed media is cached encrypted, as <name>.enc with its key in the app's own cache.
        Context app = context.getApplicationContext();
        File encrypted = file == null ? null : new File(file.getPath() + ".enc");
        if (file != null && !encrypted.isFile() && app.getExternalCacheDir() != null) {
            encrypted = new File(app.getExternalCacheDir(), file.getName() + ".enc");
        }
        if (file == null || (!file.isFile() && !encrypted.isFile())) {
            // Nothing maps the path on some renamed builds (Nekogram 12.10.5). The viewer just
            // loaded this media, so it is most likely the newest encrypted file in the cache.
            File newest = newestEncrypted(app);
            if (newest != null) {
                Logger.w("Save secret media: no path for the message, using the newest cached " + newest.getName());
                encrypted = newest;
                String name = newest.getName();
                file = new File(newest.getParentFile(), name.substring(0, name.length() - ".enc".length()));
            }
        }
        File key = file == null ? null : new File(app.getCacheDir(), file.getName() + ".enc.key");
        boolean plain = file != null && file.isFile() && file.length() > 0;
        if (!plain && (encrypted == null || !encrypted.isFile() || !key.isFile())) {
            Logger.w("Save secret media: no file at " + file);
            toast(context, Translator.get(Keys.SecretMediaNotDownloaded));
            return;
        }
        File source = file;
        File encryptedFile = encrypted;
        new Thread(() -> {
            boolean saved = copyToGallery(context, source.getName(), () -> plain
                    ? new FileInputStream(source) : decrypt(encryptedFile, key));
            new Handler(Looper.getMainLooper()).post(() ->
                    toast(context, Translator.get(saved ? Keys.SecretMediaSaved : Keys.SecretMediaSaveFailed)));
        }, "TeleVipSaveSecretMedia").start();
    }

    /** Pictures/Telegram or Movies/Telegram, where the client saves media too. */
    /** The newest <name>.enc in the external cache that has its key. */
    private static File newestEncrypted(Context app) {
        File dir = app.getExternalCacheDir();
        File[] files = dir == null ? null : dir.listFiles((d, n) -> n.endsWith(".enc"));
        if (files == null) return null;
        File newest = null;
        for (File f : files) {
            if (!new File(app.getCacheDir(), f.getName() + ".key").isFile()) continue;
            if (newest == null || f.lastModified() > newest.lastModified()) newest = f;
        }
        return newest;
    }

    private interface Source {
        InputStream open() throws Exception;
    }

    /**
     * Reads a file the client stored encrypted (EncryptedFileInputStream): AES-256-CTR, with the
     * key and IV in the key file and the IV's last four bytes counting the blocks from 0.
     */
    private static InputStream decrypt(File encrypted, File keyFile) throws Exception {
        byte[] key = new byte[32];
        byte[] iv = new byte[16];
        try (java.io.DataInputStream in = new java.io.DataInputStream(new FileInputStream(keyFile))) {
            in.readFully(key);
            in.readFully(iv);
        }
        iv[12] = iv[13] = iv[14] = iv[15] = 0;
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(key, "AES"),
                new javax.crypto.spec.IvParameterSpec(iv));
        return new javax.crypto.CipherInputStream(new FileInputStream(encrypted), cipher);
    }

    private static boolean copyToGallery(Context context, String name, Source source) {
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
                try (InputStream in = source.open(); OutputStream out = resolver.openOutputStream(uri)) {
                    if (out == null) return false;
                    copy(in, out);
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(folder), "Telegram");
                if (!dir.isDirectory() && !dir.mkdirs()) return false;
                File target = new File(dir, name);
                try (InputStream in = source.open(); OutputStream out = new FileOutputStream(target)) {
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

    private static void confirm(View button, Runnable onDelete) {
        AlertDialog dialog = new AlertDialog(button.getContext());
        dialog.setTitle(Translator.get(Keys.SecretMediaDeleteTitle));
        dialog.setMessage(Translator.get(Keys.SecretMediaDeleteMessage));
        dialog.setPositiveButton(Translator.get(Keys.Delete), AlertDialog.click(onDelete::run));
        dialog.setNegativeButton(Translator.get(Keys.Cancel), null);
        dialog.show();
    }

    private static void delete(View button, Object viewer, boolean photoViewer, long dialogId, int mid) {
        ((View) button.getParent()).setVisibility(View.GONE);
        close(viewer, photoViewer);
        KeptSecretMedia.remove(dialogId, mid);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(mid);
        if (!MessagesController.getInstance(UserConfig.getSelectedAccount()).deleteMessages(ids, dialogId, true)) {
            Logger.w("Delete for both: deleteMessages not found");
        }
    }

    private static void close(Object viewer, boolean photoViewer) {
        try {
            if (photoViewer) {
                closePhotoViewer(viewer);
            } else {
                XReflect.callMethod(viewer, AutomationResolver.resolve("SecretMediaViewer", "closePhoto", AutomationResolver.ResolverType.Method), true, false);
            }
        } catch (Throwable t) {
            Logger.e(t);
        }
    }

    /**
     * PhotoViewer.closePhoto(animated, fromEditMode) where the build keeps the name. Renamed
     * builds are not mapped for it, and need not be: PhotoViewer closes itself once the message
     * it shows is deleted.
     */
    private static void closePhotoViewer(Object viewer) throws ReflectiveOperationException {
        java.lang.reflect.Method close = XReflect.findMethodExactIfExists(viewer.getClass(), "closePhoto", boolean.class, boolean.class);
        if (close != null) close.invoke(viewer, true, false);
    }

    private static ArrayList<Object> staticInstances(Class<?> type) {
        ArrayList<Object> found = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == type) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value != null && !found.contains(value)) found.add(value);
                } catch (Throwable ignored) {
                }
            }
        }
        return found;
    }

    /** The viewer's action bar while it is on screen. R8 may narrow the field to a subclass. */
    private static View shownActionBar(Object owner, Class<?> type) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value instanceof View && ((View) value).isAttachedToWindow()
                            && ((View) value).getParent() instanceof FrameLayout) {
                        return (View) value;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }
}
