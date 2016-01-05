package com.kevenwu.refresh.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircularImageView extends ImageView {

    private final Paint maskPaint;
    private final Paint nonePaint;
    private final Paint edgePaint;
    private final int borderWidth;
    private final int borderColor;
    private int borderType;

    private Bitmap mask;

    public CircularImageView(Context context) {
        this(context, null);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int dftWidth = (int) (context.getResources().getDisplayMetrics().density);
        final int dftColor = Color.TRANSPARENT;
        final TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.CircularImageView);
        this.borderColor = ta.getColor(
                R.styleable.CircularImageView_borderColor, dftColor);
        this.borderWidth = ta.getDimensionPixelOffset(
                R.styleable.CircularImageView_borderWidth, dftWidth);
        this.borderType = ta
                .getInt(R.styleable.CircularImageView_borderType, 1);
        ta.recycle();

        this.nonePaint = new Paint();
        this.nonePaint.setAntiAlias(true);
        this.edgePaint = new Paint();
        this.edgePaint.setStyle(Paint.Style.STROKE);
        this.edgePaint.setAntiAlias(true);
        this.edgePaint.setColor(this.borderColor);
        this.edgePaint.setStrokeWidth(this.borderWidth);

        this.maskPaint = new Paint();
        this.maskPaint.setAntiAlias(true);
        this.maskPaint.setFilterBitmap(false);
        this.maskPaint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Drawable drawable = getDrawable();
        if (null == drawable)
            return;

        if (drawable instanceof NinePatchDrawable)
            return;

        final int width = getWidth();
        final int height = getHeight();
        final int layer = canvas.saveLayer(0, 0, width, height, this.nonePaint,
                Canvas.ALL_SAVE_FLAG);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        if ((null == this.mask) || (this.mask.isRecycled())) {
            this.mask = createOvalBitmap(width, height);
        }

        canvas.drawBitmap(this.mask, 0, 0, this.maskPaint);
        canvas.restoreToCount(layer);
        drawBorder(canvas, width, height);
    }

    private void drawBorder(Canvas canvas, final int width, final int height) {
        if (this.borderWidth <= 0)
            return;

        canvas.drawCircle(width >> 1, height >> 1,
                (width - this.borderWidth) >> 1, this.edgePaint);
    }

    public Bitmap createOvalBitmap(final int width, final int height) {
        final Bitmap.Config cfg = Bitmap.Config.ARGB_8888;
        final Bitmap bmp = Bitmap.createBitmap(width, height, cfg);
        final Canvas canvas = new Canvas(bmp);
        final int padding = this.borderWidth > 1 ? (this.borderWidth >> 1) : 1;
        RectF oval;
        if (borderType == 1) {
            oval = new RectF(2 * padding, 2 * padding, width - 2 * padding,
                    height - 2 * padding);
        } else {
            oval = new RectF(0, 0, width, height);
        }
        canvas.drawOval(oval, this.nonePaint);
        return bmp;
    }
}

