package com.my.televip.Drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.PathParser;

/**
 * A rounded, coloured tile with a glyph on it: the icons of TeleVip's settings groups. The glyph is
 * either Material icon path data on a 24-unit grid, drawn in white, or another drawable (the app
 * logo). Drawn in code because the module's own resources are not available inside the client.
 */
public class IconTileDrawable extends Drawable {

    /** Corner radius, as a share of the tile's side. */
    private static final float CORNER = 0.28f;
    /** The glyph's share of the tile's side: a Material icon, and a drawable such as the logo. */
    private static final float GLYPH = 0.62f, DRAWABLE = 0.86f;

    private final Paint tile = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Matrix matrix = new Matrix();
    private final Path glyphPath;
    private final Path scaled = new Path();
    private final Drawable glyph;
    private final float glyphShare;

    private IconTileDrawable(int color, Path glyphPath, Drawable glyph) {
        this.glyphPath = glyphPath;
        this.glyph = glyph;
        this.glyphShare = glyph != null ? DRAWABLE : GLYPH;
        tile.setStyle(Paint.Style.FILL);
        tile.setColor(color);
        ink.setStyle(Paint.Style.FILL);
        ink.setColor(0xFFFFFFFF);
    }

    /** A tile of {@code color} with the Material icon {@code pathData} (viewBox 0 0 24 24) on it. */
    public static IconTileDrawable material(int color, String pathData) {
        Path path;
        try {
            path = PathParser.createPathFromPathData(pathData);
        } catch (Throwable t) {
            path = null;   // a plain tile beats no row
        }
        return new IconTileDrawable(color, path, null);
    }

    /** A tile of {@code color} with {@code glyph} drawn on it. */
    public static IconTileDrawable of(int color, Drawable glyph) {
        return new IconTileDrawable(color, null, glyph);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        float side = Math.min(b.width(), b.height());
        if (side <= 0) return;
        float left = b.exactCenterX() - side / 2, top = b.exactCenterY() - side / 2;
        rect.set(left, top, left + side, top + side);
        canvas.drawRoundRect(rect, side * CORNER, side * CORNER, tile);

        float glyphSide = side * glyphShare;
        float inset = (side - glyphSide) / 2;
        if (glyphPath != null) {
            float scale = glyphSide / 24f;
            matrix.setScale(scale, scale);
            matrix.postTranslate(left + inset, top + inset);
            glyphPath.transform(matrix, scaled);
            canvas.drawPath(scaled, ink);
        } else if (glyph != null) {
            glyph.setBounds(Math.round(left + inset), Math.round(top + inset),
                    Math.round(left + inset + glyphSide), Math.round(top + inset + glyphSide));
            glyph.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        tile.setAlpha(alpha);
        ink.setAlpha(alpha);
        if (glyph != null) glyph.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        tile.setColorFilter(colorFilter);
        ink.setColorFilter(colorFilter);
        if (glyph != null) glyph.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @SuppressWarnings("deprecation")   // still abstract in Drawable
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
