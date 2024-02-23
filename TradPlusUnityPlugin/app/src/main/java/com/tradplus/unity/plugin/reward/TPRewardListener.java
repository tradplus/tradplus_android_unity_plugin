package com.tradplus.unity.plugin.reward;


public interface TPRewardListener {
    void onAdLoaded(String unitId,String tpAdInfo);

    void onAdClicked(String unitId,String tpAdInfo);

    void onAdImpression(String unitId,String tpAdInfo); // 展示 1300

    void onAdFailed(String unitId,String error);

    void onAdClosed(String unitId,String tpAdInfo); // 关闭 1400

    void onAdReward(String unitId,String tpAdInfo);

    void onAdVideoStart(String unitId,String tpAdInfo); // 播放开始，仅回调给客户 8.1

    void onAdVideoEnd(String unitId,String tpAdInfo); // 播放结束，仅回调给客户 8.1

    void onAdVideoError(String unitId,String tpAdInfo, String error);

    void onAdAllLoaded(String unitId,boolean isSuccess);
    void oneLayerLoadFailed(String unitId,String adError, String adInfo);
    void oneLayerLoaded(String unitId,String adInfo);

    //每次调用load方法时返回的回调，包含自动加载等触发时机
    void onAdStartLoad(String unitId) ;
    //每层waterfall 向三方广告源发起请求前，触发的回调
    void oneLayerLoadStart(String unitId,String tpAdInfo) ;

    void onBiddingStart(String unitId,String tpAdInfo) ;
    void onBiddingEnd(String unitId,String tpAdInfo, String adError) ;

    void onAdIsLoading(String unitId) ;


    void onDownloadStart(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);
    void onDownloadUpdate(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName,int progress);
    void onDownloadPause(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);
    void onDownloadFinish(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);
    void onDownloadFail(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);
    void onInstalled(String unitId,String tpAdInfo, long totalBytes, long currBytes, String fileName, String appName);

    void onAdAgainImpression(String unitId, String tpAdInfo);
    void onAdAgainVideoStart(String unitId,String tpAdInfo);
    void onAdAgainVideoEnd(String unitId,String tpAdInfo);
    void onAdAgainVideoClicked(String unitId,String tpAdInfo);
    void onAdPlayAgainReward(String unitId,String tpAdInfo);
}
