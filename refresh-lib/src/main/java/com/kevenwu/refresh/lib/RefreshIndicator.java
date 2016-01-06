package com.kevenwu.refresh.lib;

import android.graphics.PointF;

public class RefreshIndicator {

    public final static int POS_START = 0;
    protected int mOffsetToRefresh = 0;
    private PointF mPtLastMove = new PointF();
    private float mOffsetX;
    private float mOffsetY;
    private int mCurrentPos = 0;
    private int mLastPos = 0;
    private int mOverScrollHeight;
    private int mPressedPos = 0;

    private float mRatioOfOverScrollHeightToRefresh = 0.4f;
    private float mResistance = 1.7f;
    private boolean mIsUnderTouch = false;
    // record the refresh complete position
    private int mRefreshCompleteY = 0;

    public boolean isUnderTouch() {
        return mIsUnderTouch;
    }

    public float getResistance() {
        return mResistance;
    }

    public void setResistance(float resistance) {
        mResistance = resistance;
    }

    public void onRelease() {
        mIsUnderTouch = false;
    }

    public void onUIRefreshComplete() {
        mRefreshCompleteY = mCurrentPos;
    }

    public boolean goDownCrossFinishPosition() {
        return mCurrentPos >= mRefreshCompleteY;
    }

    protected void processOnMove(float currentX, float currentY, float offsetX, float offsetY) {
        setOffset(offsetX, offsetY / mResistance);
    }

    public void setRatioOfOverScrollHeightToRefresh(float ratio) {
        mRatioOfOverScrollHeightToRefresh = ratio;
        mOffsetToRefresh = (int) (mOverScrollHeight * ratio);
    }

    public float getRatioOfHeaderToHeightRefresh() {
        return mRatioOfOverScrollHeightToRefresh;
    }

    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }

    public void setOffsetToRefresh(int offset) {
        mRatioOfOverScrollHeightToRefresh = mOverScrollHeight * 1f / offset;
        mOffsetToRefresh = offset;
    }

    public void onPressDown(float x, float y) {
        mIsUnderTouch = true;
        mPressedPos = mCurrentPos;
        mPtLastMove.set(x, y);
    }

    public final void onMove(float x, float y) {
        float offsetX = x - mPtLastMove.x;
        float offsetY = (y - mPtLastMove.y);
        processOnMove(x, y, offsetX, offsetY);
        mPtLastMove.set(x, y);
    }

    protected void setOffset(float x, float y) {
        mOffsetX = x;
        mOffsetY = y;
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public int getLastPosY() {
        return mLastPos;
    }

    public int getCurrentPosY() {
        return mCurrentPos;
    }

    /**
     * Update current position before update the UI
     */
    public final void setCurrentPos(int current) {
        mLastPos = mCurrentPos;
        mCurrentPos = current;
        onUpdatePos(current, mLastPos);
    }

    protected void onUpdatePos(int current, int last) {

    }

    public int getHeaderHeight() {
        return mOverScrollHeight;
    }

    public void setOverScrollHeight(int height) {
        mOverScrollHeight = height;
        updateHeight();
    }

    protected void updateHeight() {
        mOffsetToRefresh = (int) (mRatioOfOverScrollHeightToRefresh * mOverScrollHeight);
    }

    public void convertFrom(RefreshIndicator ptrSlider) {
        mCurrentPos = ptrSlider.mCurrentPos;
        mLastPos = ptrSlider.mLastPos;
        mOverScrollHeight = ptrSlider.mOverScrollHeight;
    }

    public boolean hasLeftStartPosition() {
        return mCurrentPos > POS_START;
    }

    public boolean hasJustLeftStartPosition() {
        return mLastPos == POS_START && hasLeftStartPosition();
    }

    public boolean hasJustBackToStartPosition() {
        return mLastPos != POS_START && isInStartPosition();
    }

    public boolean isOverOffsetToRefresh() {
        return mCurrentPos >= getOffsetToRefresh();
    }

    public boolean hasMovedAfterPressedDown() {
        return mCurrentPos != mPressedPos;
    }

    public boolean isInStartPosition() {
        return mCurrentPos == POS_START;
    }

    public boolean crossRefreshLineFromTopToBottom() {
        return mLastPos < getOffsetToRefresh() && mCurrentPos >= getOffsetToRefresh();
    }

    public boolean hasJustReachedOverScrollHeightFromTopToBottom() {
        return mLastPos < mOverScrollHeight && mCurrentPos >= mOverScrollHeight;
    }

    public boolean isAlreadyHere(int to) {
        return mCurrentPos == to;
    }

    public float getLastPercent() {
        final float oldPercent = mOverScrollHeight == 0 ? 0 : mLastPos * 1f / mOverScrollHeight;
        return oldPercent;
    }

    public float getCurrentPercent() {
        final float currentPercent = mOverScrollHeight == 0 ? 0 : mCurrentPos * 1f / mOverScrollHeight;
        return currentPercent;
    }

    public boolean willOverTop(int to) {
        return to < POS_START;
    }
}
