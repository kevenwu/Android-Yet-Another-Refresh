package com.kevenwu.refresh.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.kevenwu.refresh.lib.util.L;

/**
 * Created by keven on 16/1/4.
 */
public class ProgressView extends FrameLayout implements RefreshInterface {

    private ImageView mLoadingView;

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
        mLoadingView = (ImageView) header.findViewById(R.id.progress_loading);
        setVisibility(INVISIBLE);
    }

    @Override
    public void onUIReset() {
        setVisibility(INVISIBLE);
    }

    @Override
    public void onUIRefreshPrepare() {
        setVisibility(VISIBLE);
    }

    @Override
    public void onUIRefreshBegin() {
        setVisibility(VISIBLE);
    }

    @Override
    public void onUIRefreshComplete() {
        setVisibility(INVISIBLE);
    }

    @Override
    public void onUIPositionChange(boolean isUnderTouch, byte status, RefreshIndicator ptrIndicator) {
        float percent = Math.min(1f, ptrIndicator.getCurrentPercent());
        L.e("percent", ""+percent);
        if (status == RefreshLayout.PTR_STATUS_PREPARE) {
//            mLoadingView.setVisibility(VISIBLE);
//            int index = (int)Math.ceil(percent * 12);
//            int resId = LODING_RES_IDS[index - 1];
            mLoadingView.setImageResource(R.drawable.loading_6);
        }
    }
}
