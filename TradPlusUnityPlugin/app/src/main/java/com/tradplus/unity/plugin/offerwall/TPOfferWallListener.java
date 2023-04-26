package com.tradplus.unity.plugin.offerwall;


public interface TPOfferWallListener {
    void onAdLoaded(String unitId,String tpAdInfo);

    void onAdClicked(String unitId,String tpAdInfo);

    void onAdImpression(String unitId,String tpAdInfo); // 展示 1300

    void onAdFailed(String unitId,String error);

    void onAdClosed(String unitId,String tpAdInfo); // 关闭 1400

    void onAdReward(String unitId,String tpAdInfo);

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


    void currencyBalanceSuccess(String unitId,int amount,String msg);
    void currencyBalanceFailed(String unitId,String msg);
    void spendCurrencySuccess(String unitId,int amount,String msg);
    void spendCurrencyFailed(String unitId,String msg);
    void awardCurrencySuccess(String unitId,int amount,String msg);
    void awardCurrencyFailed(String unitId,String msg);
    void setUserIdSuccess(String unitId);
    void setUserIdFailed(String unitId,String msg);

}
