package com.kevenwu.refresh.lib;

/**
 * Created by keven on 16/1/4.
 */
public interface RefreshInterface {
    /**
     * When the content view has reached top and refresh has been completed, view will be reset.
     */
    public void onUIReset();

    /**
     * prepare for loading
     */
    public void onUIRefreshPrepare();

    /**
     * perform refreshing UI
     */
    public void onUIRefreshBegin();

    /**
     * perform UI after refresh
     */
    public void onUIRefreshComplete();

    public void onUIPositionChange(boolean isUnderTouch, byte status, RefreshIndicator ptrIndicator);
}
