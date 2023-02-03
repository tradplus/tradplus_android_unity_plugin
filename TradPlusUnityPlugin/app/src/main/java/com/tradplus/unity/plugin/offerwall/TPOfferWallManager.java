package com.tradplus.unity.plugin.offerwall;

import android.text.TextUtils;
import android.util.Log;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.offerwall.OffWallBalanceListener;
import com.tradplus.ads.open.offerwall.OfferWallAdListener;
import com.tradplus.ads.open.offerwall.TPOfferWall;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPOfferWallManager extends BaseUnityPlugin {
    private static TPOfferWallManager sInstance;

    private TPOfferWallManager() {
    }

    public synchronized static TPOfferWallManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPOfferWallManager();
        }
        return sInstance;
    }
    // 保存广告位对象
    private Map<String, TPOfferWall> mTPOfferWall = new ConcurrentHashMap<>();

    public void loadAd(String unitId, String data, TPOfferWallListener listener){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,data,listener);

        if(tpOfferWall != null){
            tpOfferWall.loadAd();
        }

    }

    public void showAd(String unitId,String sceneId){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.showAd(getActivity(),sceneId);
        }

    }

    public void entryAdScenario(String unitId,String sceneId){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            return tpOfferWall.isReady();
        }

        return false;

    }

    public void getCurrencyBalance(String unitId){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.getCurrencyBalance();
        }

    }
    public void spendCurrency(String unitId,int balance){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.spendCurrency(balance);
        }

    }
    public void awardCurrency(String unitId,int balance){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.awardCurrency(balance);
        }

    }
    public void setUserId(String unitId,String userId){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(unitId,"");

        if(tpOfferWall != null){
            tpOfferWall.setUserId(userId);
        }

    }


    public void setCustomShowData(String adUnitId,String data){
        TPOfferWall tpOfferWall = getOrCreateOfferWall(adUnitId,"");

        if(tpOfferWall != null){
            tpOfferWall.setCustomShowData(JSON.parseObject(data));
        }
    }


    private TPOfferWall getOrCreateOfferWall(String adUnitId, String data) {
        return getOrCreateOfferWall(adUnitId,data,null);
    }
    private TPOfferWall getOrCreateOfferWall(String adUnitId, String data,TPOfferWallListener listener) {

        Log.i("tradplus","data = "+data+" mTPOfferWall = "+mTPOfferWall+" listener = "+listener);

        ExtraInfo extraInfo = null;
        if(!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();
        TPOfferWall tpOfferWall = mTPOfferWall.get(adUnitId);
        if (tpOfferWall == null) {
            tpOfferWall = new TPOfferWall(getActivity(),adUnitId);
            mTPOfferWall.put(adUnitId, tpOfferWall);

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpOfferWall.setAdListener(new TPOfferWallManager.TPOfferWallAdListener(adUnitId,listener));
            if (!isSimpleListener) {

                tpOfferWall.setAllAdLoadListener(new TPOfferWallManager.TPOfferWallAllAdListener(adUnitId, listener));
                tpOfferWall.setOffWallBalanceListener(new TPOfferWallBalanceListener(adUnitId, listener));
            }

        }
//        LogUtil.ownShow("map params = "+params);
//        // 同一个广告位每次load参数不一样，在下面设置
        if(extraInfo != null) {

            if (extraInfo.getLocalParams() != null ) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }
            if (!TextUtils.isEmpty(extraInfo.getCustomData())) {
                temp.put("custom_data", extraInfo.getCustomData());
            }
            if (!TextUtils.isEmpty(extraInfo.getUserId())) {
                temp.put("user_id", extraInfo.getUserId());
            }

            tpOfferWall.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpOfferWall;
    }

    private class TPOfferWallBalanceListener implements OffWallBalanceListener {
        private String mAdUnitId;
        private TPOfferWallListener listener;
        TPOfferWallBalanceListener(String adUnitId,TPOfferWallListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }
        

        @Override
        public void currencyBalanceSuccess(int i, String s) {
            if(listener != null){

                listener.currencyBalanceSuccess(mAdUnitId,i,s);
            }
        }

        @Override
        public void currencyBalanceFailed(String s) {
            if(listener != null){

                listener.currencyBalanceFailed(mAdUnitId,s);
            }
        }

        @Override
        public void spendCurrencySuccess(int i, String s) {
            if(listener != null){

                listener.spendCurrencySuccess(mAdUnitId,i,s);
            }
        }

        @Override
        public void spendCurrencyFailed(String s) {
            if(listener != null){

                listener.spendCurrencyFailed(mAdUnitId,s);
            }
        }

        @Override
        public void awardCurrencySuccess(int i, String s) {
            if(listener != null){

                listener.awardCurrencySuccess(mAdUnitId,i,s);
            }
        }

        @Override
        public void awardCurrencyFailed(String s) {
            if(listener != null){

                listener.awardCurrencyFailed(mAdUnitId,s);
            }
        }

        @Override
        public void setUserIdSuccess() {
            if(listener != null){

                listener.setUserIdSuccess(mAdUnitId);
            }
        }

        @Override
        public void setUserIdFailed(String s) {
            if(listener != null){

                listener.setUserIdFailed(mAdUnitId,s);
            }
        }
    }
  
    public class TPOfferWallAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPOfferWallListener listener;
        TPOfferWallAllAdListener(String adUnitId,TPOfferWallListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }
        @Override
        public void onAdAllLoaded(boolean b) {
            if(listener != null){

                listener.onAdAllLoaded(mAdUnitId,b);
            }
            Log.v("TradPlusSdk", "onAdAllLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.oneLayerLoadFailed(mAdUnitId,JSON.toJSONString(tpAdError), JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadFailed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoaded(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.oneLayerLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdStartLoad(String s) {
            if(listener != null){
                listener.onAdStartLoad(mAdUnitId);
            }
            Log.v("TradPlusSdk", "onAdStartLoad unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadStart(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.oneLayerLoadStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingStart(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onBiddingStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onBiddingStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingEnd(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if(listener != null){
                listener.onBiddingEnd(mAdUnitId, JSON.toJSONString(tpAdInfo),JSON.toJSONString(tpAdError));
            }
            Log.v("TradPlusSdk", "onBiddingEnd unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdIsLoading(String s) {
            if (listener != null) {
                listener.onAdIsLoading(mAdUnitId);
            }
            Log.v("TradPlusSdk", "onAdIsLoading unitid=" + mAdUnitId + "=======================");
        }
    }
    private class TPOfferWallAdListener implements OfferWallAdListener {
        private String mAdUnitId;
        private TPOfferWallListener listener;
        TPOfferWallAdListener(String adUnitId,TPOfferWallListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }
        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "loaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClicked(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdClicked(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdClicked unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClosed(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdClosed(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdClosed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdReward(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdReward(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdReward unitid=" + mAdUnitId + "=======================");

        }

        @Override
        public void onAdImpression(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdImpression(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdImpression unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdFailed(TPAdError tpAdError) {
            if(listener != null){
                listener.onAdFailed(mAdUnitId, JSON.toJSONString(tpAdError));
            }
        }

        @Override
        public void onAdVideoError(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if(listener != null){
                listener.onAdVideoError(mAdUnitId, JSON.toJSONString(tpAdInfo),JSON.toJSONString(tpAdError));
            }
            Log.v("TradPlusSdk", "onAdVideoError unitid=" + mAdUnitId + "=======================");
        }

    }
}
