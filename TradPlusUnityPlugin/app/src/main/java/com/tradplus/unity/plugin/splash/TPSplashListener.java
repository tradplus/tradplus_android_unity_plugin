package com.tradplus.unity.plugin.splash;

interface TPSplashListener {


    void onAdLoaded(String unitId,String var1);

    void onAdClicked(String unitId,String var1);

    void onAdImpression(String unitId,String var1);

    void onAdShowFailed(String unitId,String var1, String var2);

    void onAdLoadFailed(String unitId,String var1);

    void onAdClosed(String unitId,String var1);

    void onZoomOutStart(String unitId,String var1);

    void onZoomOutEnd(String unitId,String var1);

    void onAdAllLoaded(String unitId,boolean var1);

    void oneLayerLoadFailed(String unitId,String var1, String var2);

    void oneLayerLoaded(String unitId,String var1);

    void onAdStartLoad(String var1);

    void oneLayerLoadStart(String unitId,String var1);

    void onBiddingStart(String unitId,String var1);

    void onBiddingEnd(String unitId,String var1, String var2);

    void onAdIsLoading(String var1);

    void onDownloadStart(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadUpdate(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName, int progress);

    void onDownloadPause(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadFinish(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);

    void onDownloadFail(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);

    void onInstalled(String unitId, String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);
}
