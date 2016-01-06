package com.kevenwu.refresh.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.kevenwu.refresh.lib.util.LocalDisplay;

/**
 * Created by keven on 16/1/4.
 */
public class FloorHeaderView extends View implements RefreshInterface {

    private FloorHeaderDrawable mDrawable;

    public FloorHeaderView(Context context) {
        super(context);
        init();
    }

    public FloorHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloorHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawable = new FloorHeaderDrawable(getContext(), this);
    }

    public void setup(int height, Bitmap headerBitmap) {
        mDrawable.setUp(height, headerBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    public void onUIReset() {
        mDrawable.resetOriginals();
    }

    @Override
    public void onUIRefreshPrepare() {

    }

    @Override
    public void onUIRefreshBegin() {
        invalidate();
    }

    @Override
    public void onUIRefreshComplete() {
        invalidate();
    }

    @Override
    public void onUIPositionChange(boolean isUnderTouch, byte status, RefreshIndicator ptrIndicator) {
        float percent = ptrIndicator.getCurrentPercent();
        mDrawable.offsetTopAndBottom(ptrIndicator.getCurrentPosY());
        mDrawable.setPercent(percent);
        invalidate();
    }

    public class FloorHeaderDrawable extends Drawable {

        private static final float INITIAL_SCALE = 1.0f;

        private View mParent;
        private Matrix mMatrix;

        private int mScreenWidth;
        private int mTop;
        private float mPercent = 0.0f;
        private int mHeaderHeight;

        private Bitmap mBackground;

        private Context mContext;

        public FloorHeaderDrawable(Context context, View parent) {
            mContext = context;
            mParent = parent;

            mMatrix = new Matrix();

            initiateDimens();
        }

        private Context getContext() {
            return mContext;
        }

        private void initiateDimens() {
            LocalDisplay.init(mContext);
            mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            mTop = 0;
        }

        public void setUp(int height, Bitmap headerBitmap) {
            mHeaderHeight = height;
            if (mBackground != null) {
                mBackground.recycle();
            }
            mBackground = Bitmap.createScaledBitmap(headerBitmap, mScreenWidth, mHeaderHeight, true);
        }

        public void offsetTopAndBottom(int offset) {
            mTop = offset;
            invalidateSelf();
        }

        public void setPercent(float percent) {
            mPercent = percent;
        }

        public void resetOriginals() {
            setPercent(0);
        }

        @Override
        public void draw(Canvas canvas) {
            final int saveCount = canvas.save();
            drawBackground(canvas);
            canvas.restoreToCount(saveCount);
        }

        private void drawBackground(Canvas canvas) {
            Matrix matrix = mMatrix;
            matrix.reset();

            float scale = (float)(mTop + mHeaderHeight) / (float) mHeaderHeight;
            if (scale < INITIAL_SCALE) {
                scale = INITIAL_SCALE;
            }

            float offsetX = -(mScreenWidth * scale - mScreenWidth) / 2.0f;
//        float offsetY = -(mHeaderHeight * scale - mHeaderHeight) / 2.0f;
            matrix.postScale(scale, scale);
            matrix.postTranslate(offsetX, 0);
            if (mBackground != null) {
                canvas.drawBitmap(mBackground, matrix, null);
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
            return 0;
        }
    }
}
