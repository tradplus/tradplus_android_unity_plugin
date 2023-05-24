package com.tradplus.unity.plugin.reward;

import android.text.TextUtils;
import android.util.Log;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.base.util.SegmentUtils;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.RewardAdExListener;
import com.tradplus.ads.open.reward.RewardAdListener;
import com.tradplus.ads.open.reward.TPReward;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPRewardManager extends BaseUnityPlugin {
    private static TPRewardManager sInstance;

    private TPRewardManager() {
    }

    public synchronized static TPRewardManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPRewardManager();
        }
        return sInstance;
    }
    // 保存广告位对象
    private Map<String, TPReward> mTPReward = new ConcurrentHashMap<>();

    public void loadAd(String unitId,String data,TPRewardListener listener){
        TPReward tpReward = getTPReward(unitId,data,listener);

        if(tpReward != null){
            tpReward.loadAd();
        }

    }

    public void showAd(String unitId,String sceneId){
        TPReward tpReward = getTPReward(unitId);

        if(tpReward != null){
            tpReward.showAd(getActivity(),sceneId);
        }

    }

    public void entryAdScenario(String unitId,String sceneId){
        TPReward tpReward = getTPReward(unitId);

        if(tpReward != null){
            tpReward.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId){
        TPReward tpReward = getTPReward(unitId);

        if(tpReward != null){
            return tpReward.isReady();
        }

        return false;
    }


    public void setCustomShowData(String adUnitId,String data){
        TPReward tpReward = getTPReward(adUnitId);

        if(tpReward != null){
            tpReward.setCustomShowData(JSON.parseObject(data));
        }
    }


    private TPReward getTPReward(String adUnitId) {
        return mTPReward.get(adUnitId);
    }
    private TPReward getTPReward(String adUnitId, String data, TPRewardListener listener) {

        Log.i("tradplus","data = "+data+" mTPReward = "+mTPReward+" listener = "+listener);

        ExtraInfo extraInfo = null;
        if(!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();
        TPReward tpReward = mTPReward.get(adUnitId);
        if (tpReward == null) {
            tpReward = new TPReward(getActivity(),adUnitId);
            mTPReward.put(adUnitId, tpReward);

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpReward.setAdListener(new TPRewardAdListener(adUnitId,listener));
            tpReward.setRewardAdExListener(new TPRewardExdListener(adUnitId, listener));
            if (!isSimpleListener) {

                tpReward.setAllAdLoadListener(new TPRewardAllAdListener(adUnitId, listener));
                tpReward.setDownloadListener(new TPRewardDownloadListener(adUnitId, listener));

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

            tpReward.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpReward;
    }

    private class TPRewardExdListener implements RewardAdExListener {
        private String mAdUnitId;
        private TPRewardListener listener;
        TPRewardExdListener(String adUnitId,TPRewardListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }
        @Override
        public void onAdAgainImpression(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdAgainImpression(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdAgainImpression unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdAgainVideoStart(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdAgainVideoStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdAgainVideoStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdAgainVideoEnd(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdAgainVideoEnd(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdAgainVideoEnd unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdAgainVideoClicked(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdAgainVideoClicked(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdAgainVideoClicked unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdPlayAgainReward(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdPlayAgainReward(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdPlayAgainReward unitid=" + mAdUnitId + "=======================");
        }
    }
    private class TPRewardDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPRewardListener listener;
        TPRewardDownloadListener(String adUnitId,TPRewardListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }
        @Override
        public void onDownloadStart(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if(listener != null){
                listener.onDownloadStart(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1);
            }
            Log.v("TradPlusSdk", "onDownloadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadUpdate(TPAdInfo tpAdInfo, long l, long l1, String s, String s1, int i) {
            if(listener != null){
                listener.onDownloadUpdate(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1,i);
            }
            Log.v("TradPlusSdk", "onDownloadUpdate unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadPause(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if(listener != null){
                listener.onDownloadPause(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1);
            }
            Log.v("TradPlusSdk", "onDownloadPause unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFinish(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if(listener != null){
                listener.onDownloadFinish(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1);
            }
            Log.v("TradPlusSdk", "onDownloadFinish unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFail(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if(listener != null){
                listener.onDownloadFail(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1);
            }
            Log.v("TradPlusSdk", "onDownloadFail unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onInstalled(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if(listener != null){
                listener.onInstalled(mAdUnitId, JSON.toJSONString(tpAdInfo),l,l1,s,s1);
            }
            Log.v("TradPlusSdk", "onInstalled unitid=" + mAdUnitId + "=======================");
        }
    }
    public class TPRewardAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPRewardListener listener;
        TPRewardAllAdListener(String adUnitId,TPRewardListener listener) {
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
    private class TPRewardAdListener implements RewardAdListener {
        private String mAdUnitId;
        private TPRewardListener listener;
        TPRewardAdListener(String adUnitId,TPRewardListener listener) {
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

        @Override
        public void onAdVideoStart(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdVideoStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdVideoStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdVideoEnd(TPAdInfo tpAdInfo) {
            if(listener != null){
                listener.onAdVideoEnd(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdVideoEnd unitid=" + mAdUnitId + "=======================");
        }
    }
}
