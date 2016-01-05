package com.kevenwu.refresh.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Scroller;

import com.kevenwu.refresh.lib.util.BlurPic;
import com.kevenwu.refresh.lib.util.L;

/**
 * Created by keven on 16/1/3.
 */
public class RefreshLayout extends ViewGroup implements RefreshInterface {

    protected final String LOG_TAG = "refresh";

    public final static byte PTR_STATUS_INIT = 1;
    public final static byte PTR_STATUS_PREPARE = 2;
    public final static byte PTR_STATUS_LOADING = 3;
    public final static byte PTR_STATUS_COMPLETE = 4;
    private byte mStatus = PTR_STATUS_INIT;

    private RefreshListener mListener;

    private View mHeaderView;
    private ImageView mFakeHeaderImageView;
    private FloorHeaderView mFloorHeaderView;
    private ProgressView mProgressView;
    private ListView mListView;

    private int mHeaderHeight;
    private int mHeaderImageResId;
    private Bitmap mHeaderBitmap;
    private BitmapDrawable mHeaderDrawable;

    private MotionEvent mLastMoveEvent;
    private boolean mDisableWhenHorizontalMove = false;
    private boolean mPreventForHorizontal = false;
    private ScrollChecker mScrollChecker;
    private int mPagingTouchSlop;
    private RefreshIndicator mRefreshIndicator;
    private boolean mHasSendCancelEvent = false;

    private int mLoadingMinTime = 500;
    private long mLoadingStartTime = 0;
    private int mDurationScrollToTop = 1000;

    private Runnable mPerformRefreshCompleteDelay = new Runnable() {
        @Override
        public void run() {
            performRefreshComplete();
        }
    };

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScrollChecker = new ScrollChecker();
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        mRefreshIndicator = new RefreshIndicator();
    }

    public void setListener(RefreshListener listener) {
        mListener = listener;
    }

    public void setHeaderView(View view, ImageView fakeImageView, int headerImageResId) {
        mHeaderImageResId = headerImageResId;
        mHeaderView = view;
        mFakeHeaderImageView = fakeImageView;
        mListView.addHeaderView(view);
    }

    private void updateHeaderImage(int width, int height) {
        if(mHeaderBitmap == null) {
            mHeaderBitmap = BitmapFactory.decodeResource(getContext().getResources(), mHeaderImageResId);
            BlurPic blurPic = new BlurPic(getContext());
            mHeaderBitmap = blurPic.blur(mHeaderBitmap, 25);
            mHeaderBitmap = Bitmap.createScaledBitmap(mHeaderBitmap, width, height, true);
            mHeaderDrawable = new BitmapDrawable(getContext().getResources(), mHeaderBitmap);
            mFakeHeaderImageView.setBackgroundDrawable(mHeaderDrawable);
            showFakeHeader();
            mFloorHeaderView.setup(mHeaderHeight, mHeaderBitmap);
        }
    }

    public void autoRefresh() {
        if (mStatus != PTR_STATUS_INIT) {
            return;
        }
        mStatus = PTR_STATUS_PREPARE;
        onUIRefreshPrepare();
        mStatus = PTR_STATUS_LOADING;
        performRefresh();
    }

    final public void refreshComplete() {
        int delay = (int) (mLoadingMinTime - (System.currentTimeMillis() - mLoadingStartTime));
        if (delay <= 0) {
            performRefreshComplete();
        } else {
            postDelayed(mPerformRefreshCompleteDelay, delay);
        }
    }

    public void disableWhenHorizontalMove(boolean disable) {
        mDisableWhenHorizontalMove = disable;
    }

    @Override
    public void onUIReset() {
        L.d(LOG_TAG, "onUIReset");
        mFloorHeaderView.onUIReset();
        mProgressView.onUIReset();
    }

    @Override
    public void onUIRefreshPrepare() {
        L.d(LOG_TAG, "onUIRefreshPrepare");
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setBackgroundColor(Color.TRANSPARENT);
        mFloorHeaderView.onUIRefreshPrepare();
        mProgressView.onUIRefreshPrepare();
    }

    @Override
    public void onUIRefreshBegin() {
        L.d(LOG_TAG, "onUIRefreshBegin");
        mFloorHeaderView.onUIRefreshBegin();
        mProgressView.onUIRefreshBegin();
        if (mListener != null) {
            mListener.onRefreshBegin(this);
        }
    }

    @Override
    public void onUIRefreshComplete() {
        L.d(LOG_TAG, "onUIRefreshComplete");
        mFloorHeaderView.onUIRefreshComplete();
        mProgressView.onUIRefreshComplete();
    }

    @Override
    public void onUIPositionChange(boolean isUnderTouch, byte status, RefreshIndicator ptrIndicator) {
        mFloorHeaderView.onUIPositionChange(isUnderTouch, status, ptrIndicator);
        mProgressView.onUIPositionChange(isUnderTouch, status, ptrIndicator);
        if (ptrIndicator.getCurrentPosY() > 0) {
            hideFakeHeader();
        }
    }

    private void showFakeHeader() {
        mFakeHeaderImageView.setVisibility(VISIBLE);
    }

    private void hideFakeHeader() {
        mFakeHeaderImageView.setVisibility(INVISIBLE);
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("AprContainerLayout only host 1 elements");
        }
        View child = getChildAt(0);
        mListView = (ListView)child;
        mFloorHeaderView = new FloorHeaderView(getContext());
        mFloorHeaderView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mFloorHeaderView);
        mProgressView = new ProgressView(getContext());
        mProgressView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mProgressView);
        super.onFinishInflate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureContentView(mListView, widthMeasureSpec, heightMeasureSpec);
        measureContentView(mFloorHeaderView, widthMeasureSpec, heightMeasureSpec);
        measureContentView(mProgressView, widthMeasureSpec, heightMeasureSpec);
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        if (mHeaderHeight > 0) {
            mRefreshIndicator.setOverScrollHeight(mHeaderHeight);
            mRefreshIndicator.setRatioOfOverScrollHeightToRefresh(0.4f);
            updateHeaderImage(mHeaderView.getMeasuredWidth(), mHeaderView.getMeasuredHeight());
        }
    }

    private void measureContentView(View child,
                                    int parentWidthMeasureSpec,
                                    int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean flag, int i, int j, int k, int l) {
        layoutChildren();
    }

    private void layoutChildren() {
        int offsetY = 0;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        MarginLayoutParams lp = (MarginLayoutParams) mListView.getLayoutParams();
        final int left = paddingLeft + lp.leftMargin;
        final int top = paddingTop + lp.topMargin + offsetY;
        final int right = left + mListView.getMeasuredWidth();
        final int bottom = top + mListView.getMeasuredHeight();
        mFloorHeaderView.layout(left, top, right, bottom);
        mListView.layout(left, top, right, bottom);
        mListView.bringToFront();

        MarginLayoutParams lp2 = (MarginLayoutParams) mProgressView.getLayoutParams();
        final int left2 = paddingLeft + lp2.leftMargin;
        final int top2 = paddingTop + lp2.topMargin + offsetY;
        final int right2 = left2 + mProgressView.getMeasuredWidth();
        final int bottom2 = top2 + mProgressView.getMeasuredHeight();
        mProgressView.layout(left2, top2, right2, bottom2);
    }


    public boolean canScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return mListView.getChildCount() > 0
                    && (mListView.getFirstVisiblePosition() > 0 || mListView.getChildAt(0)
                    .getTop() < mListView.getPaddingTop());
        } else {
            return mListView.canScrollVertically(-1);
        }
    }

    public boolean checkCanDoRefresh() {
        return !canScrollUp();
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!isEnabled() || mListView == null) {
            return dispatchTouchEventSupper(e);
        }
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mRefreshIndicator.onRelease();
                if (mRefreshIndicator.hasLeftStartPosition()) {
                    onRelease(false);
                    if (mRefreshIndicator.hasMovedAfterPressedDown()) {
                        sendCancelEvent();
                        return true;
                    }
                    return dispatchTouchEventSupper(e);
                } else {
                    return dispatchTouchEventSupper(e);
                }

            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mRefreshIndicator.onPressDown(e.getX(), e.getY());

                mScrollChecker.abortIfWorking();

                mPreventForHorizontal = false;
                dispatchTouchEventSupper(e);
                return true;

            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = e;
                mRefreshIndicator.onMove(e.getX(), e.getY());
                float offsetX = mRefreshIndicator.getOffsetX();
                float offsetY = mRefreshIndicator.getOffsetY();

                if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY))) {
                    if (mRefreshIndicator.isInStartPosition()) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    showFakeHeader();
                    return dispatchTouchEventSupper(e);
                }

                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                boolean canMoveUp = mRefreshIndicator.hasLeftStartPosition();

                // disable move when header not reach top
                if (moveDown && !checkCanDoRefresh()) {
                    return dispatchTouchEventSupper(e);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    movePos(offsetY);
                    return true;
                }
                showFakeHeader();
        }
        return dispatchTouchEventSupper(e);
    }

    private void movePos(float deltaY) {
        // has reached the top
        if ((deltaY < 0 && mRefreshIndicator.isInStartPosition())) {
            return;
        }

        int to = mRefreshIndicator.getCurrentPosY() + (int) deltaY;

        // over top
        if (mRefreshIndicator.willOverTop(to)) {
            to = RefreshIndicator.POS_START;
        }

        mRefreshIndicator.setCurrentPos(to);
        int change = to - mRefreshIndicator.getLastPosY();
        updatePos(change);
    }

    private void updatePos(int change) {
        if (change == 0) {
            return;
        }

        boolean isUnderTouch = mRefreshIndicator.isUnderTouch();

        // once moved, cancel event will be sent to child
        if (isUnderTouch && !mHasSendCancelEvent && mRefreshIndicator.hasMovedAfterPressedDown()) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }

        // leave initiated position or just refresh complete
        if ((mRefreshIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) ||
                (mRefreshIndicator.goDownCrossFinishPosition() && mStatus == PTR_STATUS_COMPLETE)) {

            mStatus = PTR_STATUS_PREPARE;
            onUIRefreshPrepare();
        }

        // back to initiated position
        if (mRefreshIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();

            // recover event to children
            if (isUnderTouch) {
                sendDownEvent();
            }
        }

        // Pull to Refresh
        if (mStatus == PTR_STATUS_PREPARE) {
            // reach fresh height while moving from top to bottom
            if (isUnderTouch && mRefreshIndicator.crossRefreshLineFromTopToBottom()) {
                tryToPerformRefresh();
            }
        }

        mListView.offsetTopAndBottom(change);
        invalidate();

        onUIPositionChange(isUnderTouch, mStatus, mRefreshIndicator);
    }

    private void onRelease(boolean stayForLoading) {
        tryToPerformRefresh();

        if (mStatus == PTR_STATUS_LOADING) {
            tryScrollBackToTopWhileLoading();
        } else {
            if (mStatus == PTR_STATUS_COMPLETE) {
                notifyUIRefreshComplete(false);
            } else {
                tryScrollBackToTopAbortRefresh();
            }
        }
    }

    private void tryScrollBackToTop() {
        if (!mRefreshIndicator.isUnderTouch()) {
            mScrollChecker.tryToScrollTo(RefreshIndicator.POS_START, mDurationScrollToTop);
        }
    }

    /**
     * just make easier to understand
     */
    private void tryScrollBackToTopWhileLoading() {
        tryScrollBackToTop();
    }

    /**
     * just make easier to understand
     */
    private void tryScrollBackToTopAfterComplete() {
        tryScrollBackToTop();
    }

    /**
     * just make easier to understand
     */
    private void tryScrollBackToTopAbortRefresh() {
        tryScrollBackToTop();
    }

    private boolean tryToPerformRefresh() {
        if (mStatus != PTR_STATUS_PREPARE) {
            return false;
        }

        if (mRefreshIndicator.isOverOffsetToRefresh()) {
            mStatus = PTR_STATUS_LOADING;
            performRefresh();
        }
        return false;
    }

    private void performRefresh() {
        mLoadingStartTime = System.currentTimeMillis();
        onUIRefreshBegin();
    }

    private boolean tryToNotifyReset() {
        if ((mStatus == PTR_STATUS_COMPLETE || mStatus == PTR_STATUS_PREPARE) && mRefreshIndicator.isInStartPosition()) {
            onUIReset();
            mStatus = PTR_STATUS_INIT;
            return true;
        }
        return false;
    }

    /**
     * Do refresh complete work when time elapsed is greater than {@link #mLoadingMinTime}
     */
    private void performRefreshComplete() {
        mStatus = PTR_STATUS_COMPLETE;
        notifyUIRefreshComplete(false);
    }

    private void notifyUIRefreshComplete(boolean ignoreHook) {
        onUIRefreshComplete();
        mRefreshIndicator.onUIRefreshComplete();
        tryScrollBackToTopAfterComplete();
        tryToNotifyReset();
    }

    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    class ScrollChecker implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;

        public ScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            if (!finish) {
                mLastFlingY = curY;
                movePos(deltaY);
                post(this);
            } else {
                finish();
            }
        }

        private void finish() {
            reset();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        private void destroy() {
            reset();
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                reset();
            }
        }

        public void tryToScrollTo(int to, int duration) {
            if (mRefreshIndicator.isAlreadyHere(to)) {
                return;
            }
            mStart = mRefreshIndicator.getCurrentPosY();
            mTo = to;
            int distance = to - mStart;
            removeCallbacks(this);

            mLastFlingY = 0;

            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
            mIsRunning = true;
        }
    }

    public interface RefreshListener {
        public void onRefreshBegin(final RefreshLayout rlayout);
    }
}
