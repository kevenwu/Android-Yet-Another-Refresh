package com.kevenwu.refresh.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by keven on 16/1/4.
 */
public class ProgressView extends FrameLayout implements RefreshInterface {

    private ImageView mRotateView;
    private ProgressBar mProgressBar;
    private TextView mTextView;

    int[] LODING_RES_IDS = {
            R.drawable.loading_1,
            R.drawable.loading_2,
            R.drawable.loading_3,
            R.drawable.loading_4,
            R.drawable.loading_5,
            R.drawable.loading_6,
            R.drawable.loading_7,
            R.drawable.loading_8,
            R.drawable.loading_9,
            R.drawable.loading_10,
            R.drawable.loading_11,
            R.drawable.loading_12,
    };

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.progress_layout, this);
        mRotateView = (ImageView) header.findViewById(R.id.progress_loading);
        mProgressBar = (ProgressBar) header.findViewById(R.id.progressbar_loading);
        mTextView = (TextView) header.findViewById(R.id.progress_title);
        setVisibility(INVISIBLE);
    }

    @Override
    public void onUIReset() {
        setVisibility(INVISIBLE);
    }

    @Override
    public void onUIRefreshPrepare() {
        setVisibility(VISIBLE);
        mProgressBar.setVisibility(VISIBLE);
        mRotateView.setVisibility(INVISIBLE);
        mTextView.setText(R.string.pull_down_to_refresh);
    }

    @Override
    public void onUIRefreshBegin() {
        setVisibility(VISIBLE);
        mProgressBar.setVisibility(VISIBLE);
        mRotateView.setVisibility(INVISIBLE);
        mTextView.setText(R.string.refreshing);
    }

    @Override
    public void onUIRefreshComplete() {
        mProgressBar.setVisibility(INVISIBLE);
        mRotateView.setVisibility(INVISIBLE);
        mTextView.setText(R.string.refresh_complete);
    }

    @Override
    public void onUIPositionChange(boolean isUnderTouch, byte status, RefreshIndicator ptrIndicator) {
        float percent = Math.min(1f, ptrIndicator.getCurrentPercent() / ptrIndicator.getRatioOfHeaderToHeightRefresh());
        if (status == RefreshLayout.PTR_STATUS_PREPARE) {
            mProgressBar.setVisibility(INVISIBLE);
            mRotateView.setVisibility(VISIBLE);
            int index = (int)(Math.ceil(percent * 12)) - 1;
            int resId = LODING_RES_IDS[index];
            mRotateView.setImageResource(resId);
        }

        final int mOffsetToRefresh = ptrIndicator.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();
        final int lastPos = ptrIndicator.getLastPosY();
        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == RefreshLayout.PTR_STATUS_PREPARE) {
                mTextView.setText(R.string.pull_down_to_refresh);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (isUnderTouch && status == RefreshLayout.PTR_STATUS_PREPARE) {
                mTextView.setText(R.string.release_to_refresh);
            }
        }
    }
}
