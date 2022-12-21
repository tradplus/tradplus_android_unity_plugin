package com.tradplus.unity.plugin.interstitial;

import android.text.TextUtils;
import android.util.Log;

import com.tradplus.ads.base.bean.TPAdError;
import com.tradplus.ads.base.bean.TPAdInfo;
import com.tradplus.ads.common.serialization.JSON;
import com.tradplus.ads.mobileads.util.SegmentUtils;
import com.tradplus.ads.open.DownloadListener;
import com.tradplus.ads.open.LoadAdEveryLayerListener;
import com.tradplus.ads.open.banner.TPBanner;
import com.tradplus.ads.open.interstitial.InterstitialAdListener;
import com.tradplus.ads.open.interstitial.TPInterstitial;
import com.tradplus.unity.plugin.common.BaseUnityPlugin;
import com.tradplus.unity.plugin.common.ExtraInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPInterstitialManager extends BaseUnityPlugin {
    private static TPInterstitialManager sInstance;

    private TPInterstitialManager() {
    }

    public synchronized static TPInterstitialManager getInstance() {
        if (sInstance == null) {
            sInstance = new TPInterstitialManager();
        }
        return sInstance;
    }

    // 保存广告位对象
    private Map<String, TPInterstitial> mTPInterstitial = new ConcurrentHashMap<>();

    public void loadAd(String unitId, String data, TPInterstitialListener listener) {
        TPInterstitial tpInterstitial = getOrCreateInterstitial(unitId, data, listener);

        if (tpInterstitial != null) {
            tpInterstitial.loadAd();
        }

    }

    public void showAd(String unitId, String sceneId) {
        TPInterstitial tpInterstitial = getOrCreateInterstitial(unitId, "");

        if (tpInterstitial != null) {
            tpInterstitial.showAd(getActivity(), sceneId);
        }

    }

    public void entryAdScenario(String unitId, String sceneId) {
        TPInterstitial tpInterstitial = getOrCreateInterstitial(unitId, "");

        if (tpInterstitial != null) {
            tpInterstitial.entryAdScenario(sceneId);
        }

    }

    public boolean isReady(String unitId) {
        TPInterstitial tpInterstitial = getOrCreateInterstitial(unitId, "");

        if (tpInterstitial != null) {
            return tpInterstitial.isReady();
        }

        return false;
    }


    public void setCustomShowData(String adUnitId, String data) {
        TPInterstitial tpInterstitial = getOrCreateInterstitial(adUnitId, "");

        if (tpInterstitial != null) {
            tpInterstitial.setCustomShowData(JSON.parseObject(data));
        }
    }


    private TPInterstitial getOrCreateInterstitial(String adUnitId, String data) {
        return getOrCreateInterstitial(adUnitId, data, null);
    }

    private TPInterstitial getOrCreateInterstitial(String adUnitId, String data, TPInterstitialListener listener) {

        Log.i("tradplus", "data = " + data + " mTPInterstitial = " + mTPInterstitial + " listener = " + listener);

        ExtraInfo extraInfo = null;
        if (!TextUtils.isEmpty(data)) {
            extraInfo = JSON.parseObject(data, ExtraInfo.class);
        }

        HashMap<String, Object> temp = new HashMap<>();

        TPInterstitial tpInterstitial = mTPInterstitial.get(adUnitId);
        if (tpInterstitial == null) {
            tpInterstitial = new TPInterstitial(getActivity(), adUnitId);
            mTPInterstitial.put(adUnitId, tpInterstitial);

            boolean isSimpleListener = extraInfo == null ? false : extraInfo.isSimpleListener();

            tpInterstitial.setAdListener(new TPInterstitialAdListener(adUnitId, listener));
            if (!isSimpleListener) {
                tpInterstitial.setAllAdLoadListener(new TPInterstitialdAllAdListener(adUnitId, listener));
                tpInterstitial.setDownloadListener(new TPInterstitialDownloadListener(adUnitId, listener));
            }

        }

        if (extraInfo != null) {

            if (extraInfo.getLocalParams() != null) {
                temp = (HashMap<String, Object>) extraInfo.getLocalParams();
            }
            if (!TextUtils.isEmpty(extraInfo.getCustomData())) {
                temp.put("custom_data", extraInfo.getCustomData());
            }
            if (!TextUtils.isEmpty(extraInfo.getUserId())) {
                temp.put("user_id", extraInfo.getUserId());
            }

            tpInterstitial.setCustomParams(temp);

            if (extraInfo.getCustomMap() != null) {
                SegmentUtils.initPlacementCustomMap(adUnitId, extraInfo.getCustomMap());
            }
        }

        return tpInterstitial;
    }


    private class TPInterstitialDownloadListener implements DownloadListener {
        private String mAdUnitId;
        private TPInterstitialListener listener;

        TPInterstitialDownloadListener(String adUnitId, TPInterstitialListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onDownloadStart(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadStart(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadUpdate(TPAdInfo tpAdInfo, long l, long l1, String s, String s1, int i) {
            if (listener != null) {
                listener.onDownloadUpdate(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1, i);
            }
            Log.v("TradPlusSdk", "onDownloadUpdate unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadPause(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadPause(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadPause unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFinish(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadFinish(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadFinish unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onDownloadFail(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onDownloadFail(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onDownloadFail unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onInstalled(TPAdInfo tpAdInfo, long l, long l1, String s, String s1) {
            if (listener != null) {
                listener.onInstalled(mAdUnitId, JSON.toJSONString(tpAdInfo), l, l1, s, s1);
            }
            Log.v("TradPlusSdk", "onInstalled unitid=" + mAdUnitId + "=======================");
        }
    }

    public class TPInterstitialdAllAdListener implements LoadAdEveryLayerListener {
        private String mAdUnitId;
        private TPInterstitialListener listener;

        TPInterstitialdAllAdListener(String adUnitId, TPInterstitialListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdAllLoaded(boolean b) {
            if (listener != null) {

                listener.onAdAllLoaded(mAdUnitId, b);
            }
            Log.v("TradPlusSdk", "onAdAllLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadFailed(TPAdError tpAdError, TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoadFailed(mAdUnitId, JSON.toJSONString(tpAdError), JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadFailed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoaded(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdStartLoad(String s) {
            if (listener != null) {
                listener.onAdStartLoad(mAdUnitId);
            }
            Log.v("TradPlusSdk", "onAdStartLoad unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void oneLayerLoadStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.oneLayerLoadStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "oneLayerLoadStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onBiddingStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onBiddingStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onBiddingEnd(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if (listener != null) {
                listener.onBiddingEnd(mAdUnitId, JSON.toJSONString(tpAdInfo), JSON.toJSONString(tpAdError));
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

    private class TPInterstitialAdListener implements InterstitialAdListener {
        private String mAdUnitId;
        private TPInterstitialListener listener;

        TPInterstitialAdListener(String adUnitId, TPInterstitialListener listener) {
            mAdUnitId = adUnitId;
            this.listener = listener;
        }

        @Override
        public void onAdLoaded(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdLoaded(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "loaded unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClicked(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdClicked(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdClicked unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdClosed(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdClosed(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdClosed unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdImpression(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdImpression(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdImpression unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdFailed(TPAdError tpAdError) {
            if (listener != null) {
                listener.onAdFailed(mAdUnitId, JSON.toJSONString(tpAdError));
            }
        }

        @Override
        public void onAdVideoError(TPAdInfo tpAdInfo, TPAdError tpAdError) {
            if (listener != null) {
                listener.onAdVideoError(mAdUnitId, JSON.toJSONString(tpAdInfo), JSON.toJSONString(tpAdError));
            }
            Log.v("TradPlusSdk", "onAdVideoError unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdVideoStart(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdVideoStart(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdVideoStart unitid=" + mAdUnitId + "=======================");
        }

        @Override
        public void onAdVideoEnd(TPAdInfo tpAdInfo) {
            if (listener != null) {
                listener.onAdVideoEnd(mAdUnitId, JSON.toJSONString(tpAdInfo));
            }
            Log.v("TradPlusSdk", "onAdVideoEnd unitid=" + mAdUnitId + "=======================");
        }
    }
}
