package com.my.televip.settings.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.my.televip.application.AndroidUtilities;
import com.my.televip.virtuals.Theme;

/**
 * The frosted-glass look of TeleVip's settings: a fixed backdrop of soft colour glows that the
 * cards scroll over, and translucent cards with a light edge. Everything is drawn with plain
 * gradients, so it looks the same on every Android version the module runs on: a real backdrop
 * blur needs Android 12, and blurring glows that are already soft would add nothing.
 */
final class GlassStyle {

    private GlassStyle() {
    }

    /** The screen's background, behind the toolbar and the list. */
    static Drawable backdrop() {
        return new Backdrop(Theme.isLight(), Theme.getBackgroundGrayColor(), Theme.getTextBlueColor());
    }

    /** The toolbar's background: its colour, letting the glows show through a little. */
    static int toolbarColor() {
        return withAlpha(Theme.getToolBarColor(), Theme.isLight() ? 0xB8 : 0xC4);
    }

    /** A card: the row colour, mostly opaque so text stays easy to read, with a lit edge. */
    static GradientDrawable card() {
        boolean light = Theme.isLight();
        int row = Theme.getBackgroundWhiteOrBlueColor();
        GradientDrawable card = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                withAlpha(row, light ? 0xEB : 0xE0),
                withAlpha(row, light ? 0xC7 : 0xC2)});
        card.setCornerRadius(AndroidUtilities.dp(20));
        card.setStroke(Math.max(1, AndroidUtilities.dp(1) / 2 + 1), light ? 0xB3FFFFFF : 0x12FFFFFF);
        return card;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /** {@code color} with its hue turned by {@code degrees}. */
    private static int turnHue(int color, float degrees) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = (hsv[0] + degrees + 360f) % 360f;
        return Color.HSVToColor(hsv);
    }

    /** The base colour with three large, faint glows of the accent and two hues next to it. */
    private static final class Backdrop extends Drawable {

        private final int base;
        private final int[] glows;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);

        Backdrop(boolean light, int base, int accent) {
            this.base = base;
            int alpha = light ? 0x5C : 0x38;
            glows = new int[]{
                    withAlpha(accent, alpha),
                    withAlpha(turnHue(accent, 55), alpha),
                    withAlpha(turnHue(accent, -45), alpha * 4 / 5)};
            paint.setColor(base);
        }

        /** The glows' centres and radii, as shares of the width and height, and their shaders. */
        private static final float[][] SPOTS = {{0.10f, 0.06f, 0.85f}, {0.95f, 0.45f, 0.80f}, {0.20f, 0.92f, 0.75f}};
        private final Shader[] shaders = new Shader[SPOTS.length];
        private final float[][] circles = new float[SPOTS.length][3];

        @Override
        protected void onBoundsChange(Rect b) {
            float w = b.width(), h = b.height();
            float r = Math.max(w, h * 0.6f);
            // Top start, middle end, bottom start: one is always behind some card.
            for (int i = 0; i < SPOTS.length; i++) {
                float x = b.left + w * SPOTS[i][0], y = b.top + h * SPOTS[i][1], radius = r * SPOTS[i][2];
                circles[i][0] = x;
                circles[i][1] = y;
                circles[i][2] = radius;
                int color = glows[i];
                shaders[i] = radius <= 0 ? null : new RadialGradient(x, y, radius,
                        new int[]{color, withAlpha(color, Color.alpha(color) / 3), withAlpha(color, 0)},
                        new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRect(getBounds(), paint);
            for (int i = 0; i < SPOTS.length; i++) {
                if (shaders[i] == null) continue;
                glow.setShader(shaders[i]);
                canvas.drawCircle(circles[i][0], circles[i][1], circles[i][2], glow);
            }
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return Color.alpha(base) == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        }
    }
}
